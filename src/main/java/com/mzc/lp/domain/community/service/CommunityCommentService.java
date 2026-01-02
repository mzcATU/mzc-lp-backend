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
import com.mzc.lp.domain.community.repository.CommunityCommentLikeRepository;
import com.mzc.lp.domain.community.repository.CommunityCommentRepository;
import com.mzc.lp.domain.community.repository.CommunityPostRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }

        // 대댓글인 경우 부모 댓글 존재 확인
        if (request.parentId() != null) {
            if (!commentRepository.existsById(request.parentId())) {
                throw new CommentNotFoundException(request.parentId());
            }
        }

        CommunityComment comment = CommunityComment.create(postId, userId, request.content(), request.parentId());
        CommunityComment savedComment = commentRepository.save(comment);

        User author = userRepository.findById(userId).orElse(null);
        return CommentResponse.from(savedComment, author, 0, false);
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
        if (!commentRepository.findByIdAndPostId(commentId, postId).isPresent()) {
            throw new CommentNotFoundException(commentId);
        }

        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new AlreadyLikedException("Already liked comment: " + commentId);
        }

        CommunityCommentLike like = CommunityCommentLike.create(commentId, userId);
        commentLikeRepository.save(like);
    }

    @Transactional
    public void unlikeComment(Long userId, Long postId, Long commentId) {
        if (!commentRepository.findByIdAndPostId(commentId, postId).isPresent()) {
            throw new CommentNotFoundException(commentId);
        }

        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
    }
}
