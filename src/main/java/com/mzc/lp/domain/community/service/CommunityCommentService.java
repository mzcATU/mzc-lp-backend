package com.mzc.lp.domain.community.service;

import com.mzc.lp.domain.community.dto.request.CreateCommentRequest;
import com.mzc.lp.domain.community.dto.request.UpdateCommentRequest;
import com.mzc.lp.domain.community.dto.response.CommentListResponse;
import com.mzc.lp.domain.community.dto.response.CommentResponse;
import com.mzc.lp.domain.community.entity.CommunityComment;
import com.mzc.lp.domain.community.entity.CommunityCommentLike;
import com.mzc.lp.domain.community.exception.AlreadyLikedException;
import com.mzc.lp.domain.community.exception.CommentNotFoundException;
import com.mzc.lp.domain.community.exception.NotCommentAuthorException;
import com.mzc.lp.domain.community.exception.PostNotFoundException;
import com.mzc.lp.domain.community.entity.CommunityPost;
import com.mzc.lp.domain.community.repository.CommunityCommentLikeRepository;
import com.mzc.lp.domain.community.repository.CommunityCommentRepository;
import com.mzc.lp.domain.community.repository.CommunityPostRepository;
import com.mzc.lp.domain.notification.constant.NotificationType;
import com.mzc.lp.domain.notification.service.NotificationService;
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
public class CommunityCommentService {

    private final CommunityCommentRepository commentRepository;
    private final CommunityCommentLikeRepository commentLikeRepository;
    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentListResponse getComments(Long postId, int page, int pageSize, Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<CommunityComment> commentPage = commentRepository.findTopLevelCommentsByPostId(postId, pageable);

        List<CommunityComment> topLevelComments = commentPage.getContent();

        if (topLevelComments.isEmpty()) {
            return CommentListResponse.of(List.of(), 0, page, pageSize, 0);
        }

        // 대댓글 벌크 조회
        List<Long> topLevelIds = topLevelComments.stream().map(CommunityComment::getId).toList();
        List<CommunityComment> allReplies = commentRepository.findByParentIdIn(topLevelIds);

        // 대댓글을 부모별로 그룹핑
        Map<Long, List<CommunityComment>> repliesByParentId = allReplies.stream()
                .collect(Collectors.groupingBy(CommunityComment::getParentId));

        // 모든 댓글 ID 수집 (좋아요 조회용)
        Set<Long> allCommentIds = new HashSet<>(topLevelIds);
        allReplies.forEach(reply -> allCommentIds.add(reply.getId()));

        // 작성자 벌크 조회
        Set<Long> authorIds = new HashSet<>();
        topLevelComments.forEach(c -> authorIds.add(c.getAuthorId()));
        allReplies.forEach(c -> authorIds.add(c.getAuthorId()));
        Map<Long, User> authorMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 좋아요 여부 벌크 조회
        Set<Long> likedCommentIds = userId != null
                ? new HashSet<>(commentLikeRepository.findLikedCommentIdsByUserIdAndCommentIdIn(userId, new ArrayList<>(allCommentIds)))
                : Set.of();

        // 응답 변환
        List<CommentResponse> commentResponses = topLevelComments.stream()
                .map(comment -> {
                    User author = authorMap.get(comment.getAuthorId());
                    long likeCount = commentLikeRepository.countByCommentId(comment.getId());
                    boolean isLiked = likedCommentIds.contains(comment.getId());

                    // 대댓글 변환
                    List<CommentResponse> replies = repliesByParentId.getOrDefault(comment.getId(), List.of())
                            .stream()
                            .map(reply -> {
                                User replyAuthor = authorMap.get(reply.getAuthorId());
                                long replyLikeCount = commentLikeRepository.countByCommentId(reply.getId());
                                boolean replyIsLiked = likedCommentIds.contains(reply.getId());
                                return CommentResponse.from(reply, replyAuthor, replyLikeCount, replyIsLiked);
                            })
                            .toList();

                    return CommentResponse.from(comment, author, likeCount, isLiked, replies);
                })
                .toList();

        return CommentListResponse.of(
                commentResponses,
                commentPage.getTotalElements(),
                page,
                pageSize,
                commentPage.getTotalPages()
        );
    }

    @Transactional
    public CommentResponse createComment(Long userId, Long postId, CreateCommentRequest request) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 대댓글인 경우 부모 댓글 존재 확인
        CommunityComment parentComment = null;
        if (request.parentId() != null) {
            parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new CommentNotFoundException(request.parentId()));
        }

        CommunityComment comment = CommunityComment.create(postId, userId, request.content(), request.parentId());
        CommunityComment savedComment = commentRepository.save(comment);

        User author = userRepository.findById(userId).orElse(null);
        String authorName = author != null ? author.getName() : "알 수 없음";

        // 알림 생성: 게시글 작성자에게 (본인 제외)
        if (!post.getAuthorId().equals(userId)) {
            notificationService.createNotification(
                    post.getAuthorId(),
                    NotificationType.COMMENT,
                    "새 댓글이 달렸습니다",
                    authorName + "님이 \"" + truncate(post.getTitle(), 20) + "\" 게시글에 댓글을 남겼습니다.",
                    "/tu/b2c/community/" + postId,
                    postId,
                    "POST",
                    userId,
                    authorName
            );
        }

        // 대댓글인 경우: 부모 댓글 작성자에게도 알림 (본인 및 게시글 작성자 제외)
        if (parentComment != null && !parentComment.getAuthorId().equals(userId)
                && !parentComment.getAuthorId().equals(post.getAuthorId())) {
            notificationService.createNotification(
                    parentComment.getAuthorId(),
                    NotificationType.COMMENT,
                    "대댓글이 달렸습니다",
                    authorName + "님이 회원님의 댓글에 답글을 남겼습니다.",
                    "/tu/b2c/community/" + postId,
                    savedComment.getId(),
                    "COMMENT",
                    userId,
                    authorName
            );
        }

        return CommentResponse.from(savedComment, author, 0, false);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long postId, Long commentId, UpdateCommentRequest request) {
        CommunityComment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getAuthorId().equals(userId)) {
            throw new NotCommentAuthorException(commentId);
        }

        comment.update(request.content());

        User author = userRepository.findById(comment.getAuthorId()).orElse(null);
        long likeCount = commentLikeRepository.countByCommentId(commentId);
        boolean isLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);

        return CommentResponse.from(comment, author, likeCount, isLiked);
    }

    @Transactional
    public void deleteComment(Long userId, Long postId, Long commentId) {
        CommunityComment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getAuthorId().equals(userId)) {
            throw new NotCommentAuthorException(commentId);
        }

        // 대댓글인 경우 해당 댓글만 삭제
        // 부모 댓글인 경우 대댓글도 함께 삭제
        if (comment.getParentId() == null) {
            List<CommunityComment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
            List<Long> replyIds = replies.stream().map(CommunityComment::getId).toList();
            if (!replyIds.isEmpty()) {
                commentLikeRepository.deleteByCommentIdIn(replyIds);
                commentRepository.deleteAll(replies);
            }
        }

        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
        commentRepository.delete(comment);
    }

    @Transactional
    public void likeComment(Long userId, Long postId, Long commentId) {
        CommunityComment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new AlreadyLikedException("Already liked comment: " + commentId);
        }

        try {
            CommunityCommentLike like = CommunityCommentLike.create(commentId, userId);
            commentLikeRepository.save(like);
            commentLikeRepository.flush(); // 즉시 DB에 반영하여 제약조건 위반 확인

            // 알림 생성: 댓글 작성자에게 (본인 제외)
            if (!comment.getAuthorId().equals(userId)) {
                User actor = userRepository.findById(userId).orElse(null);
                String actorName = actor != null ? actor.getName() : "알 수 없음";

                notificationService.createNotification(
                        comment.getAuthorId(),
                        NotificationType.LIKE,
                        "댓글에 좋아요를 받았습니다",
                        actorName + "님이 회원님의 댓글을 좋아합니다.",
                        "/tu/b2c/community/" + postId,
                        commentId,
                        "COMMENT_LIKE",
                        userId,
                        actorName
                );
            }
        } catch (DataIntegrityViolationException e) {
            // 동시성 이슈로 인한 중복 좋아요 시도
            log.debug("Concurrent comment like attempt detected for comment {} by user {}", commentId, userId);
            throw new AlreadyLikedException("Already liked comment: " + commentId);
        }
    }

    @Transactional
    public void unlikeComment(Long userId, Long postId, Long commentId) {
        if (!commentRepository.findByIdAndPostId(commentId, postId).isPresent()) {
            throw new CommentNotFoundException(commentId);
        }

        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
    }
}
