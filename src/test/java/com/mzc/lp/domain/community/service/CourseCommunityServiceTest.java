package com.mzc.lp.domain.community.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.community.constant.PostType;
import com.mzc.lp.domain.community.dto.request.CreateCoursePostRequest;
import com.mzc.lp.domain.community.dto.request.UpdatePostRequest;
import com.mzc.lp.domain.community.dto.response.CategoryResponse;
import com.mzc.lp.domain.community.dto.response.PostDetailResponse;
import com.mzc.lp.domain.community.dto.response.PostListResponse;
import com.mzc.lp.domain.community.dto.response.PostResponse;
import com.mzc.lp.domain.community.entity.CommunityPost;
import com.mzc.lp.domain.community.exception.NotEnrolledException;
import com.mzc.lp.domain.community.exception.NotPostAuthorException;
import com.mzc.lp.domain.community.exception.PostNotFoundException;
import com.mzc.lp.domain.community.repository.CommunityCommentRepository;
import com.mzc.lp.domain.community.repository.CommunityPostLikeRepository;
import com.mzc.lp.domain.community.repository.CommunityPostRepository;
import com.mzc.lp.domain.notification.service.NotificationService;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseCommunityService 테스트")
class CourseCommunityServiceTest extends TenantTestSupport {

    protected static final Long TENANT_ID = DEFAULT_TENANT_ID;

    @InjectMocks
    private CourseCommunityService courseCommunityService;

    @Mock
    private CommunityPostRepository postRepository;

    @Mock
    private CommunityPostLikeRepository postLikeRepository;

    @Mock
    private CommunityCommentRepository commentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    private CommunityPost createTestPost(Long id, Long courseTimeId, Long authorId) {
        CommunityPost post = CommunityPost.createForCourse(
                PostType.QUESTION,
                "question",
                "테스트 제목",
                "테스트 내용",
                authorId,
                "tag1,tag2",
                courseTimeId,
                false
        );
        // Reflection을 사용하여 id 설정 (테스트용)
        // CommunityPost -> TenantEntity -> BaseTimeEntity -> BaseEntity (id 필드 위치)
        try {
            var idField = CommunityPost.class.getSuperclass().getSuperclass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(post, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    private User createTestUser(Long id, String name) {
        User user = User.create(
                "test" + id + "@test.com",
                name,
                "encodedPassword123"
        );
        // Reflection을 사용하여 id 설정 (테스트용)
        // User -> TenantEntity -> BaseTimeEntity -> BaseEntity (id 필드 위치)
        try {
            var idField = User.class.getSuperclass().getSuperclass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    // ==================== 게시글 목록 조회 테스트 ====================

    @Nested
    @DisplayName("getPosts - 코스 커뮤니티 게시글 목록 조회")
    class GetPosts {

        @Test
        @DisplayName("성공 - 게시글 목록 조회")
        void getPosts_success() {
            // given
            Long courseTimeId = 1L;
            Long userId = 100L;
            int page = 0, pageSize = 20;

            List<CommunityPost> posts = List.of(
                    createTestPost(1L, courseTimeId, userId),
                    createTestPost(2L, courseTimeId, 101L)
            );
            Page<CommunityPost> postPage = new PageImpl<>(posts, PageRequest.of(page, pageSize), posts.size());

            given(postRepository.findByCourseTimeIdWithFilters(
                    eq(courseTimeId), isNull(), isNull(), isNull(), any(Pageable.class)))
                    .willReturn(postPage);

            given(userRepository.findAllById(anySet()))
                    .willReturn(List.of(createTestUser(userId, "테스트유저"), createTestUser(101L, "유저2")));

            given(postLikeRepository.findLikedPostIdsByUserIdAndPostIdIn(eq(userId), anyList()))
                    .willReturn(List.of());

            given(postLikeRepository.countByPostId(anyLong())).willReturn(0L);
            given(commentRepository.countByPostId(anyLong())).willReturn(0L);

            // when
            PostListResponse response = courseCommunityService.getPosts(
                    courseTimeId, null, null, null, "latest", page, pageSize, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.posts()).hasSize(2);
        }
    }

    // ==================== 게시글 작성 테스트 ====================

    @Nested
    @DisplayName("createPost - 코스 커뮤니티 게시글 작성")
    class CreatePost {

        @Test
        @DisplayName("성공 - 수강생이 게시글 작성")
        void createPost_success() {
            // given
            Long courseTimeId = 1L;
            Long userId = 100L;

            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, TENANT_ID))
                    .willReturn(true);

            CreateCoursePostRequest request = new CreateCoursePostRequest(
                    PostType.QUESTION,
                    "테스트 제목",
                    "테스트 내용",
                    "question",
                    List.of("tag1", "tag2"),
                    false
            );

            CommunityPost savedPost = createTestPost(1L, courseTimeId, userId);
            given(postRepository.save(any(CommunityPost.class))).willReturn(savedPost);
            given(userRepository.findById(userId)).willReturn(Optional.of(createTestUser(userId, "테스트유저")));

            // when
            PostResponse response = courseCommunityService.createPost(courseTimeId, userId, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.courseTimeId()).isEqualTo(courseTimeId);
            verify(postRepository).save(any(CommunityPost.class));
        }

        @Test
        @DisplayName("실패 - 수강생이 아닌 경우")
        void createPost_fail_notEnrolled() {
            // given
            Long courseTimeId = 1L;
            Long userId = 100L;

            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, TENANT_ID))
                    .willReturn(false);

            CreateCoursePostRequest request = new CreateCoursePostRequest(
                    PostType.QUESTION,
                    "테스트 제목",
                    "테스트 내용",
                    "question",
                    null,
                    false
            );

            // when & then
            assertThatThrownBy(() -> courseCommunityService.createPost(courseTimeId, userId, request))
                    .isInstanceOf(NotEnrolledException.class);
        }
    }

    // ==================== 게시글 상세 조회 테스트 ====================

    @Nested
    @DisplayName("getPost - 코스 커뮤니티 게시글 상세 조회")
    class GetPost {

        @Test
        @DisplayName("성공 - 게시글 상세 조회")
        void getPost_success() {
            // given
            Long courseTimeId = 1L;
            Long postId = 1L;
            Long userId = 100L;

            CommunityPost post = createTestPost(postId, courseTimeId, userId);
            given(postRepository.findByIdAndCourseTimeId(postId, courseTimeId))
                    .willReturn(Optional.of(post));

            given(userRepository.findById(userId)).willReturn(Optional.of(createTestUser(userId, "테스트유저")));
            given(postLikeRepository.countByPostId(postId)).willReturn(5L);
            given(commentRepository.countByPostId(postId)).willReturn(3L);
            given(postLikeRepository.existsByPostIdAndUserId(postId, userId)).willReturn(true);

            // when
            PostDetailResponse response = courseCommunityService.getPost(courseTimeId, postId, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(postId);
            assertThat(response.courseTimeId()).isEqualTo(courseTimeId);
            assertThat(response.likeCount()).isEqualTo(5L);
            assertThat(response.commentCount()).isEqualTo(3L);
            assertThat(response.isLiked()).isTrue();
            verify(postRepository).incrementViewCount(postId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void getPost_fail_notFound() {
            // given
            Long courseTimeId = 1L;
            Long postId = 999L;
            Long userId = 100L;

            given(postRepository.findByIdAndCourseTimeId(postId, courseTimeId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> courseCommunityService.getPost(courseTimeId, postId, userId))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    // ==================== 게시글 수정 테스트 ====================

    @Nested
    @DisplayName("updatePost - 코스 커뮤니티 게시글 수정")
    class UpdatePost {

        @Test
        @DisplayName("성공 - 작성자가 게시글 수정")
        void updatePost_success() {
            // given
            Long courseTimeId = 1L;
            Long postId = 1L;
            Long userId = 100L;

            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, TENANT_ID))
                    .willReturn(true);

            CommunityPost post = createTestPost(postId, courseTimeId, userId);
            given(postRepository.findByIdAndCourseTimeId(postId, courseTimeId))
                    .willReturn(Optional.of(post));

            given(userRepository.findById(userId)).willReturn(Optional.of(createTestUser(userId, "테스트유저")));
            given(postLikeRepository.countByPostId(postId)).willReturn(0L);
            given(commentRepository.countByPostId(postId)).willReturn(0L);
            given(postLikeRepository.existsByPostIdAndUserId(postId, userId)).willReturn(false);

            UpdatePostRequest request = new UpdatePostRequest(
                    "수정된 제목",
                    "수정된 내용",
                    "question",
                    List.of("newtag"),
                    false
            );

            // when
            PostResponse response = courseCommunityService.updatePost(courseTimeId, userId, postId, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("수정된 제목");
        }

        @Test
        @DisplayName("실패 - 작성자가 아닌 경우")
        void updatePost_fail_notAuthor() {
            // given
            Long courseTimeId = 1L;
            Long postId = 1L;
            Long userId = 100L;
            Long anotherUserId = 200L;

            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(anotherUserId, courseTimeId, TENANT_ID))
                    .willReturn(true);

            CommunityPost post = createTestPost(postId, courseTimeId, userId);
            given(postRepository.findByIdAndCourseTimeId(postId, courseTimeId))
                    .willReturn(Optional.of(post));

            UpdatePostRequest request = new UpdatePostRequest(
                    "수정된 제목",
                    "수정된 내용",
                    "question",
                    null,
                    false
            );

            // when & then
            assertThatThrownBy(() -> courseCommunityService.updatePost(courseTimeId, anotherUserId, postId, request))
                    .isInstanceOf(NotPostAuthorException.class);
        }
    }

    // ==================== 게시글 삭제 테스트 ====================

    @Nested
    @DisplayName("deletePost - 코스 커뮤니티 게시글 삭제")
    class DeletePost {

        @Test
        @DisplayName("성공 - 작성자가 게시글 삭제")
        void deletePost_success() {
            // given
            Long courseTimeId = 1L;
            Long postId = 1L;
            Long userId = 100L;

            given(enrollmentRepository.existsByUserIdAndCourseTimeIdAndTenantId(userId, courseTimeId, TENANT_ID))
                    .willReturn(true);

            CommunityPost post = createTestPost(postId, courseTimeId, userId);
            given(postRepository.findByIdAndCourseTimeId(postId, courseTimeId))
                    .willReturn(Optional.of(post));

            // when
            courseCommunityService.deletePost(courseTimeId, userId, postId);

            // then
            verify(postLikeRepository).deleteByPostId(postId);
            verify(commentRepository).deleteByPostId(postId);
            verify(postRepository).delete(post);
        }
    }

    // ==================== 카테고리 조회 테스트 ====================

    @Nested
    @DisplayName("getCategories - 코스 커뮤니티 카테고리 조회")
    class GetCategories {

        @Test
        @DisplayName("성공 - 카테고리 목록 조회")
        void getCategories_success() {
            // given
            Long courseTimeId = 1L;
            Long userId = 100L;

            given(postRepository.countByCourseTimeId(courseTimeId)).willReturn(10L);
            given(postRepository.countByCourseTimeIdAndCategory(courseTimeId, "question")).willReturn(5L);
            given(postRepository.countByCourseTimeIdAndCategory(courseTimeId, "tip")).willReturn(3L);
            given(postRepository.countByCourseTimeIdAndCategory(courseTimeId, "discussion")).willReturn(2L);

            // when
            CategoryResponse response = courseCommunityService.getCategories(courseTimeId, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.categories()).hasSize(4);
        }
    }
}
