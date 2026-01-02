package com.mzc.lp.domain.community.service;

import com.mzc.lp.domain.community.constant.PostType;
import com.mzc.lp.domain.community.dto.request.CreatePostRequest;
import com.mzc.lp.domain.community.dto.request.UpdatePostRequest;
import com.mzc.lp.domain.community.dto.response.*;
import com.mzc.lp.domain.community.entity.CommunityPost;
import com.mzc.lp.domain.community.entity.CommunityPostLike;
import com.mzc.lp.domain.community.exception.AlreadyLikedException;
import com.mzc.lp.domain.community.exception.NotPostAuthorException;
import com.mzc.lp.domain.community.exception.PostNotFoundException;
import com.mzc.lp.domain.community.repository.CommunityCommentRepository;
import com.mzc.lp.domain.community.repository.CommunityPostLikeRepository;
import com.mzc.lp.domain.community.repository.CommunityPostRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final CommunityPostLikeRepository postLikeRepository;
    private final CommunityCommentRepository commentRepository;
    private final UserRepository userRepository;

    public PostListResponse getPosts(String keyword, String category, String type, String sortBy, int page, int pageSize, Long userId) {
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
            postPage = postRepository.findByFiltersOrderByPopular(keywordFilter, categoryFilter, postType, pageable);
        } else {
            postPage = postRepository.findByFilters(keywordFilter, categoryFilter, postType, pageable);
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
        Set<Long> likedPostIds = userId != null
                ? new HashSet<>(postLikeRepository.findLikedPostIdsByUserIdAndPostIdIn(userId, postIds))
                : Set.of();

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

    @Transactional
    public PostDetailResponse getPost(Long postId, Long userId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 조회수 증가
        postRepository.incrementViewCount(postId);
        // 증가된 조회수를 반영하기 위해 엔티티의 viewCount도 증가
        post.incrementViewCount();

        User author = userRepository.findById(post.getAuthorId()).orElse(null);
        long likeCount = postLikeRepository.countByPostId(postId);
        long commentCount = commentRepository.countByPostId(postId);
        boolean isLiked = userId != null && postLikeRepository.existsByPostIdAndUserId(postId, userId);

        return PostDetailResponse.from(post, author, likeCount, commentCount, isLiked);
    }

    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        CommunityPost post = CommunityPost.create(
                request.type(),
                request.category(),
                request.title(),
                request.content(),
                userId,
                request.tagsAsString()
        );

        CommunityPost savedPost = postRepository.save(post);

        User author = userRepository.findById(userId).orElse(null);
        return PostResponse.from(savedPost, author, 0, 0, false);
    }

    @Transactional
    public PostResponse updatePost(Long userId, Long postId, UpdatePostRequest request) {
        CommunityPost post = postRepository.findById(postId)
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

    @Transactional
    public void deletePost(Long userId, Long postId) {
        CommunityPost post = postRepository.findById(postId)
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

    @Transactional
    public void likePost(Long userId, Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new AlreadyLikedException("Already liked post: " + postId);
        }

        try {
            CommunityPostLike like = CommunityPostLike.create(postId, userId);
            postLikeRepository.save(like);
            postLikeRepository.flush(); // 즉시 DB에 반영하여 제약조건 위반 확인
        } catch (DataIntegrityViolationException e) {
            // 동시성 이슈로 인한 중복 좋아요 시도
            log.debug("Concurrent like attempt detected for post {} by user {}", postId, userId);
            throw new AlreadyLikedException("Already liked post: " + postId);
        }
    }

    @Transactional
    public void unlikePost(Long userId, Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }

        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    public PostListResponse getPopularPosts(int limit, Long userId) {
        Pageable pageable = PageRequest.of(0, limit);
        List<CommunityPost> posts = postRepository.findPopularPosts(pageable);

        if (posts.isEmpty()) {
            return PostListResponse.of(List.of(), 0, 0, limit, 0);
        }

        Set<Long> authorIds = posts.stream().map(CommunityPost::getAuthorId).collect(Collectors.toSet());
        Map<Long, User> authorMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> postIds = posts.stream().map(CommunityPost::getId).toList();
        Set<Long> likedPostIds = userId != null
                ? new HashSet<>(postLikeRepository.findLikedPostIdsByUserIdAndPostIdIn(userId, postIds))
                : Set.of();

        List<PostResponse> postResponses = posts.stream()
                .map(post -> {
                    User author = authorMap.get(post.getAuthorId());
                    long likeCount = postLikeRepository.countByPostId(post.getId());
                    long commentCount = commentRepository.countByPostId(post.getId());
                    boolean isLiked = likedPostIds.contains(post.getId());
                    return PostResponse.from(post, author, likeCount, commentCount, isLiked);
                })
                .toList();

        return PostListResponse.of(postResponses, posts.size(), 0, limit, 1);
    }

    public CategoryResponse getCategories() {
        // 하드코딩된 카테고리 목록
        List<CategoryResponse.CategoryItem> categories = List.of(
                CategoryResponse.CategoryItem.of("all", "전체", null, postRepository.count(), "grid"),
                CategoryResponse.CategoryItem.of("question", "Q&A", null, postRepository.countByCategory("question"), "help-circle"),
                CategoryResponse.CategoryItem.of("tip", "학습 팁", null, postRepository.countByCategory("tip"), "lightbulb"),
                CategoryResponse.CategoryItem.of("review", "강의 후기", null, postRepository.countByCategory("review"), "star"),
                CategoryResponse.CategoryItem.of("discussion", "스터디 모집", null, postRepository.countByCategory("discussion"), "users")
        );

        return CategoryResponse.of(categories);
    }

    /**
     * 내 게시글 목록 조회
     */
    public PostListResponse getMyPosts(Long userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<CommunityPost> postPage = postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable);

        List<CommunityPost> posts = postPage.getContent();

        if (posts.isEmpty()) {
            return PostListResponse.of(List.of(), 0, page, pageSize, 0);
        }

        User author = userRepository.findById(userId).orElse(null);

        List<PostResponse> postResponses = posts.stream()
                .map(post -> {
                    long likeCount = postLikeRepository.countByPostId(post.getId());
                    long commentCount = commentRepository.countByPostId(post.getId());
                    boolean isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), userId);
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
     * 내가 댓글 단 게시글 목록 조회
     */
    public PostListResponse getCommentedPosts(Long userId, int page, int pageSize) {
        // 내가 댓글 단 게시글 ID 목록 조회
        List<Long> commentedPostIds = commentRepository.findDistinctPostIdsByAuthorId(userId);

        if (commentedPostIds.isEmpty()) {
            return PostListResponse.of(List.of(), 0, page, pageSize, 0);
        }

        // 페이징 처리
        int start = page * pageSize;
        int end = Math.min(start + pageSize, commentedPostIds.size());

        if (start >= commentedPostIds.size()) {
            return PostListResponse.of(List.of(), commentedPostIds.size(), page, pageSize, (int) Math.ceil((double) commentedPostIds.size() / pageSize));
        }

        List<Long> pagedPostIds = commentedPostIds.subList(start, end);

        // 게시글 조회
        List<CommunityPost> posts = postRepository.findAllById(pagedPostIds);

        // 순서 유지를 위해 재정렬
        Map<Long, CommunityPost> postMap = posts.stream()
                .collect(Collectors.toMap(CommunityPost::getId, Function.identity()));
        posts = pagedPostIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .toList();

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

        int totalPages = (int) Math.ceil((double) commentedPostIds.size() / pageSize);

        return PostListResponse.of(
                postResponses,
                commentedPostIds.size(),
                page,
                pageSize,
                totalPages
        );
    }
}
