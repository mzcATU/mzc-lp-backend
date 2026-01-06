package com.mzc.lp.domain.community.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.community.constant.PostType;
import com.mzc.lp.domain.community.dto.request.CreateCoursePostRequest;
import com.mzc.lp.domain.community.dto.request.UpdatePostRequest;
import com.mzc.lp.domain.community.dto.response.*;
import com.mzc.lp.domain.community.entity.CommunityPost;
import com.mzc.lp.domain.community.entity.CommunityPostLike;
import com.mzc.lp.domain.community.exception.AlreadyLikedException;
import com.mzc.lp.domain.community.exception.NotEnrolledException;
import com.mzc.lp.domain.community.exception.NotPostAuthorException;
import com.mzc.lp.domain.community.exception.PostNotFoundException;
import com.mzc.lp.domain.community.repository.CommunityCommentRepository;
import com.mzc.lp.domain.community.repository.CommunityPostLikeRepository;
import com.mzc.lp.domain.community.repository.CommunityPostRepository;
import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.service.NotificationService;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 코스(차수) 단위 커뮤니티 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseCommunityService {

    private final CommunityPostRepository postRepository;
    private final CommunityPostLikeRepository postLikeRepository;
    private final CommunityCommentRepository commentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 수강 여부 검증
     */
    private void validateEnrollment(Long courseTimeId, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(
                userId, courseTimeId, tenantId);
        if (!isEnrolled) {
            throw new NotEnrolledException(courseTimeId);
        }
    }

    /**
     * 코스 커뮤니티 게시글 목록 조회
     */
    public PostListResponse getPosts(Long courseTimeId, String keyword, String category,
                                     String type, String sortBy, int page, int pageSize, Long userId) {
        // 수강 검증
        validateEnrollment(courseTimeId, userId);

        PostType postType = null;
        if (type != null && !type.equalsIgnoreCase("all")) {
            try {
                postType = PostType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        String categoryFilter = (category != null && !category.equalsIgnoreCase("all")) ? category : null;
        String keywordFilter = (keyword != null && !keyword.isBlank()) ? keyword : null;

        Pageable pageable = PageRequest.of(page, pageSize);

        Page<CommunityPost> postPage;
        if ("popular".equalsIgnoreCase(sortBy)) {
            postPage = postRepository.findByCourseTimeIdWithFiltersOrderByPopular(
                    courseTimeId, keywordFilter, categoryFilter, postType, pageable);
        } else {
            postPage = postRepository.findByCourseTimeIdWithFilters(
                    courseTimeId, keywordFilter, categoryFilter, postType, pageable);
        }

        List<CommunityPost> posts = postPage.getContent();

        if (posts.isEmpty()) {
            return PostListResponse.of(List.of(), 0, page, pageSize, 0);
        }

        // 작성자 벌크 조회
        Set<Long> authorIds = posts.stream().map(CommunityPost::getAuthorId).collect(Collectors.toSet());
        Map<Long, User> authorMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 좋아요 여부 벌크 조회
        List<Long> postIds = posts.stream().map(CommunityPost::getId).toList();
        Set<Long> likedPostIds = new HashSet<>(postLikeRepository.findLikedPostIdsByUserIdAndPostIdIn(userId, postIds));

        // 응답 변환
        List<PostResponse> postResponses = posts.stream()
                .map(post -> {
                    User author = authorMap.get(post.getAuthorId());
                    long likeCount = postLikeRepository.countByPostId(post.getId());
                    long commentCount = commentRepository.countByPostId(post.getId());
                    boolean isLiked = likedPostIds.contains(post.getId());
                    return PostResponse.from(post, author, likeCount, commentCount, isLiked);
                })
                .toList();

        return PostListResponse.of(
                postResponses,
                postPage.getTotalElements(),
                page,
                pageSize,
                postPage.getTotalPages()
        );
    }

    /**
     * 코스 커뮤니티 게시글 상세 조회
     */
    @Transactional
    public PostDetailResponse getPost(Long courseTimeId, Long postId, Long userId) {
        // 수강 검증
        validateEnrollment(courseTimeId, userId);

        CommunityPost post = postRepository.findByIdAndCourseTimeId(postId, courseTimeId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 조회수 증가
        postRepository.incrementViewCount(postId);
        post.incrementViewCount();

        User author = userRepository.findById(post.getAuthorId()).orElse(null);
        long likeCount = postLikeRepository.countByPostId(postId);
        long commentCount = commentRepository.countByPostId(postId);
        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId);

        return PostDetailResponse.from(post, author, likeCount, commentCount, isLiked);
    }

    /**
     * 코스 커뮤니티 게시글 작성
     */
    @Transactional
    public PostResponse createPost(Long courseTimeId, Long userId, CreateCoursePostRequest request) {
        // 수강 검증
        validateEnrollment(courseTimeId, userId);

        CommunityPost post = CommunityPost.createForCourse(
                request.type(),
                request.category(),
                request.title(),
                request.content(),
                userId,
                request.tagsAsString(),
                courseTimeId
        );

        CommunityPost savedPost = postRepository.save(post);

        User author = userRepository.findById(userId).orElse(null);
        return PostResponse.from(savedPost, author, 0, 0, false);
    }

    /**
     * 코스 커뮤니티 게시글 수정
     */
    @Transactional
    public PostResponse updatePost(Long courseTimeId, Long userId, Long postId, UpdatePostRequest request) {
        // 수강 검증
        validateEnrollment(courseTimeId, userId);

        CommunityPost post = postRepository.findByIdAndCourseTimeId(postId, courseTimeId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.getAuthorId().equals(userId)) {
            throw new NotPostAuthorException(postId);
        }

        post.update(request.title(), request.content(), request.category(), request.tagsAsString());

        User author = userRepository.findById(post.getAuthorId()).orElse(null);
        long likeCount = postLikeRepository.countByPostId(postId);
        long commentCount = commentRepository.countByPostId(postId);
        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId);

        return PostResponse.from(post, author, likeCount, commentCount, isLiked);
    }

    /**
     * 코스 커뮤니티 게시글 삭제
     */
    @Transactional
    public void deletePost(Long courseTimeId, Long userId, Long postId) {
        // 수강 검증
        validateEnrollment(courseTimeId, userId);

        CommunityPost post = postRepository.findByIdAndCourseTimeId(postId, courseTimeId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.getAuthorId().equals(userId)) {
            throw new NotPostAuthorException(postId);
        }

        // 관련 좋아요 삭제
        postLikeRepository.deleteByPostId(postId);
        // 관련 댓글 삭제
        commentRepository.deleteByPostId(postId);
        // 게시글 삭제
        postRepository.delete(post);
    }

    /**
     * 코스 커뮤니티 게시글 좋아요
     */
    @Transactional
    public void likePost(Long courseTimeId, Long userId, Long postId) {
        // 수강 검증
        validateEnrollment(courseTimeId, userId);

        CommunityPost post = postRepository.findByIdAndCourseTimeId(postId, courseTimeId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new AlreadyLikedException("Already liked post: " + postId);
        }

        try {
            CommunityPostLike like = CommunityPostLike.create(postId, userId);
            postLikeRepository.save(like);
            postLikeRepository.flush();

            // 알림 생성: 게시글 작성자에게 (본인 제외)
            if (!post.getAuthorId().equals(userId)) {
                User actor = userRepository.findById(userId).orElse(null);
                String actorName = actor != null ? actor.getName() : "알 수 없음";

                notificationService.createNotification(
                        post.getAuthorId(),
                        NotificationType.LIKE,
                        "게시글에 좋아요를 받았습니다",
                        actorName + "님이 \"" + truncate(post.getTitle(), 20) + "\" 게시글을 좋아합니다.",
                        "/tu/b2c/times/" + courseTimeId + "/community/" + postId,
                        postId,
                        "COURSE_POST_LIKE",
                        userId,
                        actorName
                );
            }
        } catch (DataIntegrityViolationException e) {
            log.debug("Concurrent like attempt detected for post {} by user {}", postId, userId);
            throw new AlreadyLikedException("Already liked post: " + postId);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    /**
     * 코스 커뮤니티 게시글 좋아요 취소
     */
    @Transactional
    public void unlikePost(Long courseTimeId, Long userId, Long postId) {
        // 수강 검증
        validateEnrollment(courseTimeId, userId);

        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }

        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    /**
     * 코스 커뮤니티 카테고리 목록 조회
     */
    public CategoryResponse getCategories(Long courseTimeId, Long userId) {
        // 수강 검증
        validateEnrollment(courseTimeId, userId);

        List<CategoryResponse.CategoryItem> categories = List.of(
                CategoryResponse.CategoryItem.of("all", "전체", null,
                        postRepository.countByCourseTimeId(courseTimeId), "grid"),
                CategoryResponse.CategoryItem.of("question", "Q&A", null,
                        postRepository.countByCourseTimeIdAndCategory(courseTimeId, "question"), "help-circle"),
                CategoryResponse.CategoryItem.of("tip", "학습 팁", null,
                        postRepository.countByCourseTimeIdAndCategory(courseTimeId, "tip"), "lightbulb"),
                CategoryResponse.CategoryItem.of("discussion", "스터디/토론", null,
                        postRepository.countByCourseTimeIdAndCategory(courseTimeId, "discussion"), "users")
        );

        return CategoryResponse.of(categories);
    }
}
