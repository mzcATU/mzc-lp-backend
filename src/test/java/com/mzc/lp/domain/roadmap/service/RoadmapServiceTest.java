package com.mzc.lp.domain.roadmap.service;

import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.program.exception.ProgramNotFoundException;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import com.mzc.lp.domain.roadmap.dto.request.CreateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.request.SaveDraftRequest;
import com.mzc.lp.domain.roadmap.dto.request.UpdateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapDetailResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapResponse;
import com.mzc.lp.domain.roadmap.dto.response.RoadmapStatisticsResponse;
import com.mzc.lp.domain.roadmap.entity.Roadmap;
import com.mzc.lp.domain.roadmap.entity.RoadmapProgram;
import com.mzc.lp.domain.roadmap.exception.*;
import com.mzc.lp.domain.roadmap.repository.RoadmapProgramRepository;
import com.mzc.lp.domain.roadmap.repository.RoadmapRepository;
import com.mzc.lp.domain.user.constant.CourseRole;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoadmapService 테스트")
class RoadmapServiceTest {

    @InjectMocks
    private RoadmapServiceImpl roadmapService;

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private RoadmapProgramRepository roadmapProgramRepository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private UserCourseRoleRepository userCourseRoleRepository;

    @Test
    @DisplayName("로드맵 생성 - 성공")
    void createRoadmap_Success() {
        // given
        Long authorId = 1L;
        List<Long> programIds = Arrays.asList(1L, 2L);
        CreateRoadmapRequest request = new CreateRoadmapRequest(
                "테스트 로드맵",
                "테스트 설명",
                programIds,
                RoadmapStatus.DRAFT
        );

        Program program1 = mock(Program.class);
        Program program2 = mock(Program.class);

        given(programRepository.findById(1L)).willReturn(Optional.of(program1));
        given(programRepository.findById(2L)).willReturn(Optional.of(program2));
        given(userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(authorId, CourseRole.DESIGNER))
                .willReturn(true);

        Roadmap savedRoadmap = Roadmap.create("테스트 로드맵", "테스트 설명", authorId, RoadmapStatus.DRAFT);
        given(roadmapRepository.save(any(Roadmap.class))).willReturn(savedRoadmap);

        // when
        RoadmapResponse response = roadmapService.createRoadmap(request, authorId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("테스트 로드맵");
        assertThat(response.status()).isEqualTo("draft");
        assertThat(response.courseCount()).isEqualTo(2);

        verify(roadmapRepository).save(any(Roadmap.class));
        verify(roadmapProgramRepository, times(2)).save(any(RoadmapProgram.class));
    }

    @Test
    @DisplayName("로드맵 생성 - 중복 프로그램 ID 실패")
    void createRoadmap_DuplicateProgramIds_Fail() {
        // given
        Long authorId = 1L;
        List<Long> programIds = Arrays.asList(1L, 1L); // 중복
        CreateRoadmapRequest request = new CreateRoadmapRequest(
                "테스트 로드맵",
                "테스트 설명",
                programIds,
                RoadmapStatus.DRAFT
        );

        // when & then
        assertThatThrownBy(() -> roadmapService.createRoadmap(request, authorId))
                .isInstanceOf(DuplicateProgramInRoadmapException.class);

        verify(roadmapRepository, never()).save(any());
    }

    @Test
    @DisplayName("로드맵 생성 - 프로그램 존재하지 않음 실패")
    void createRoadmap_ProgramNotFound_Fail() {
        // given
        Long authorId = 1L;
        List<Long> programIds = Arrays.asList(999L);
        CreateRoadmapRequest request = new CreateRoadmapRequest(
                "테스트 로드맵",
                "테스트 설명",
                programIds,
                RoadmapStatus.DRAFT
        );

        given(programRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roadmapService.createRoadmap(request, authorId))
                .isInstanceOf(ProgramNotFoundException.class);

        verify(roadmapRepository, never()).save(any());
    }

    @Test
    @DisplayName("로드맵 생성 - 권한 없음 실패")
    void createRoadmap_NoPermission_Fail() {
        // given
        Long authorId = 1L;
        List<Long> programIds = Arrays.asList(1L);
        CreateRoadmapRequest request = new CreateRoadmapRequest(
                "테스트 로드맵",
                "테스트 설명",
                programIds,
                RoadmapStatus.DRAFT
        );

        Program program = mock(Program.class);
        given(programRepository.findById(1L)).willReturn(Optional.of(program));
        given(userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(authorId, CourseRole.DESIGNER))
                .willReturn(false);
        given(userCourseRoleRepository.existsByUserIdAndCourseIdAndRole(authorId, 1L, CourseRole.DESIGNER))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> roadmapService.createRoadmap(request, authorId))
                .isInstanceOf(InvalidProgramException.class);

        verify(roadmapRepository, never()).save(any());
    }

    @Test
    @DisplayName("로드맵 상세 조회 - 성공")
    void getRoadmap_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        Roadmap roadmap = Roadmap.create("테스트 로드맵", "테스트 설명", authorId, RoadmapStatus.PUBLISHED);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));
        given(roadmapProgramRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmapId))
                .willReturn(Arrays.asList());

        // when
        RoadmapDetailResponse response = roadmapService.getRoadmap(roadmapId, authorId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("테스트 로드맵");
        verify(roadmapRepository).findById(roadmapId);
    }

    @Test
    @DisplayName("로드맵 상세 조회 - 존재하지 않음 실패")
    void getRoadmap_NotFound_Fail() {
        // given
        Long roadmapId = 999L;
        Long authorId = 1L;

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roadmapService.getRoadmap(roadmapId, authorId))
                .isInstanceOf(RoadmapNotFoundException.class);
    }

    @Test
    @DisplayName("로드맵 상세 조회 - DRAFT 상태 권한 없음 실패")
    void getRoadmap_DraftNoPermission_Fail() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        Long otherUserId = 2L;
        Roadmap roadmap = Roadmap.create("테스트 로드맵", "테스트 설명", authorId, RoadmapStatus.DRAFT);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));

        // when & then
        assertThatThrownBy(() -> roadmapService.getRoadmap(roadmapId, otherUserId))
                .isInstanceOf(RoadmapOwnershipException.class);
    }

    @Test
    @DisplayName("내 로드맵 목록 조회 - 성공")
    void getMyRoadmaps_Success() {
        // given
        Long authorId = 1L;
        Pageable pageable = PageRequest.of(0, 20);

        Roadmap roadmap1 = Roadmap.create("로드맵1", "설명1", authorId, RoadmapStatus.DRAFT);
        Roadmap roadmap2 = Roadmap.create("로드맵2", "설명2", authorId, RoadmapStatus.PUBLISHED);
        Page<Roadmap> roadmapPage = new PageImpl<>(Arrays.asList(roadmap1, roadmap2));

        given(roadmapRepository.findByAuthorIdOrderByUpdatedAtDesc(authorId, pageable))
                .willReturn(roadmapPage);
        given(roadmapProgramRepository.countByRoadmapId(any())).willReturn(3);

        // when
        Page<RoadmapResponse> response = roadmapService.getMyRoadmaps(authorId, null, "updatedAt", pageable);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).title()).isEqualTo("로드맵1");
        assertThat(response.getContent().get(0).courseCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("로드맵 수정 - 성공")
    void updateRoadmap_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        List<Long> programIds = Arrays.asList(1L, 2L);
        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "수정된 제목",
                "수정된 설명",
                programIds,
                RoadmapStatus.PUBLISHED
        );

        Roadmap roadmap = Roadmap.create("원본 제목", "원본 설명", authorId, RoadmapStatus.DRAFT);
        Program program1 = mock(Program.class);
        Program program2 = mock(Program.class);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));
        given(programRepository.findById(1L)).willReturn(Optional.of(program1));
        given(programRepository.findById(2L)).willReturn(Optional.of(program2));
        given(userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(authorId, CourseRole.DESIGNER))
                .willReturn(true);
        given(roadmapProgramRepository.countByRoadmapId(roadmapId)).willReturn(2);

        // when
        RoadmapResponse response = roadmapService.updateRoadmap(roadmapId, request, authorId);

        // then
        assertThat(response.title()).isEqualTo("수정된 제목");
        assertThat(response.status()).isEqualTo("published");
        verify(roadmapProgramRepository).deleteByRoadmapId(roadmapId);
        verify(roadmapProgramRepository, times(2)).save(any(RoadmapProgram.class));
    }

    @Test
    @DisplayName("로드맵 수정 - 권한 없음 실패")
    void updateRoadmap_NoOwnership_Fail() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        Long otherUserId = 2L;
        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "수정된 제목",
                null,
                null,
                null
        );

        Roadmap roadmap = Roadmap.create("원본 제목", "원본 설명", authorId, RoadmapStatus.DRAFT);
        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));

        // when & then
        assertThatThrownBy(() -> roadmapService.updateRoadmap(roadmapId, request, otherUserId))
                .isInstanceOf(RoadmapOwnershipException.class);
    }

    @Test
    @DisplayName("로드맵 수정 - PUBLISHED 상태에서도 수정 가능")
    void updateRoadmap_PublishedStatus_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "수정된 제목",
                "수정된 설명",
                Arrays.asList(1L, 2L),
                RoadmapStatus.PUBLISHED
        );

        Roadmap roadmap = Roadmap.create("원본 제목", "원본 설명", authorId, RoadmapStatus.PUBLISHED);
        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));

        Program program1 = mock(Program.class);
        Program program2 = mock(Program.class);
        given(programRepository.findById(1L)).willReturn(Optional.of(program1));
        given(programRepository.findById(2L)).willReturn(Optional.of(program2));
        given(userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(authorId, CourseRole.DESIGNER))
                .willReturn(true);
        given(roadmapProgramRepository.countByRoadmapId(roadmapId)).willReturn(2);

        // when
        RoadmapResponse response = roadmapService.updateRoadmap(roadmapId, request, authorId);

        // then
        assertThat(response).isNotNull();
        assertThat(roadmap.getTitle()).isEqualTo("수정된 제목");
        assertThat(roadmap.getDescription()).isEqualTo("수정된 설명");
        assertThat(roadmap.getStatus()).isEqualTo(RoadmapStatus.PUBLISHED);
        verify(roadmapProgramRepository).deleteByRoadmapId(roadmapId);
        verify(roadmapProgramRepository, times(2)).save(any(RoadmapProgram.class));
    }

    @Test
    @DisplayName("임시저장 - 빈 프로그램 목록으로 성공")
    void saveDraft_EmptyProgramIds_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        SaveDraftRequest request = new SaveDraftRequest(
                "임시저장 제목",
                "임시저장 설명",
                Arrays.asList() // 빈 목록
        );

        Roadmap roadmap = Roadmap.create("원본 제목", "원본 설명", authorId, RoadmapStatus.DRAFT);
        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));
        given(roadmapProgramRepository.countByRoadmapId(roadmapId)).willReturn(0);

        // when
        RoadmapResponse response = roadmapService.saveDraft(roadmapId, request, authorId);

        // then
        assertThat(response.title()).isEqualTo("임시저장 제목");
        assertThat(response.courseCount()).isEqualTo(0);
        verify(roadmapProgramRepository).deleteByRoadmapId(roadmapId);
        verify(roadmapProgramRepository, never()).save(any());
    }

    @Test
    @DisplayName("로드맵 삭제 - 성공")
    void deleteRoadmap_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        Roadmap roadmap = Roadmap.create("테스트 로드맵", "테스트 설명", authorId, RoadmapStatus.DRAFT);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));

        // when
        roadmapService.deleteRoadmap(roadmapId, authorId);

        // then
        verify(roadmapRepository).delete(roadmap);
    }

    @Test
    @DisplayName("로드맵 삭제 - 권한 없음 실패")
    void deleteRoadmap_NoOwnership_Fail() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        Long otherUserId = 2L;
        Roadmap roadmap = Roadmap.create("테스트 로드맵", "테스트 설명", authorId, RoadmapStatus.DRAFT);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));

        // when & then
        assertThatThrownBy(() -> roadmapService.deleteRoadmap(roadmapId, otherUserId))
                .isInstanceOf(RoadmapOwnershipException.class);

        verify(roadmapRepository, never()).delete(any());
    }

    @Test
    @DisplayName("로드맵 복제 - 성공")
    void duplicateRoadmap_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        Long currentUserId = 2L;

        Roadmap originalRoadmap = Roadmap.create("원본 로드맵", "원본 설명", authorId, RoadmapStatus.PUBLISHED);
        Program program1 = mock(Program.class);
        RoadmapProgram roadmapProgram1 = RoadmapProgram.create(originalRoadmap, program1, 0);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(originalRoadmap));
        given(roadmapProgramRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmapId))
                .willReturn(Arrays.asList(roadmapProgram1));
        given(roadmapRepository.save(any(Roadmap.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        RoadmapResponse response = roadmapService.duplicateRoadmap(roadmapId, currentUserId);

        // then
        assertThat(response.title()).contains("원본 로드맵");
        assertThat(response.title()).contains("복사본");
        assertThat(response.status()).isEqualTo("draft");
        assertThat(response.courseCount()).isEqualTo(1);

        verify(roadmapRepository).save(any(Roadmap.class));
        verify(roadmapProgramRepository).save(any(RoadmapProgram.class));
    }

    @Test
    @DisplayName("통계 조회 - 성공")
    void getStatistics_Success() {
        // given
        Long authorId = 1L;

        given(roadmapRepository.countByAuthorId(authorId)).willReturn(5L);
        given(roadmapRepository.sumEnrolledStudentsByAuthorId(authorId)).willReturn(100L);
        given(roadmapRepository.getAverageCourseCountByAuthorId(authorId)).willReturn(3.5);

        // when
        RoadmapStatisticsResponse response = roadmapService.getStatistics(authorId);

        // then
        assertThat(response.totalRoadmaps()).isEqualTo(5L);
        assertThat(response.totalEnrollments()).isEqualTo(100L);
        assertThat(response.averageCourseCount()).isEqualTo(3.5);
    }

    // ===== Safe vs Destructive Update Tests =====

    @Test
    @DisplayName("로드맵 수정 - Safe Update: 메타데이터만 변경 (성공)")
    void updateRoadmap_SafeUpdate_MetadataOnly_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "수정된 제목",
                "수정된 설명",
                null, // 프로그램 변경 없음
                RoadmapStatus.PUBLISHED
        );

        Roadmap roadmap = Roadmap.create("원본 제목", "원본 설명", authorId, RoadmapStatus.PUBLISHED);
        ReflectionTestUtils.setField(roadmap, "enrolledStudents", 10); // 수강생 있음
        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));
        given(roadmapProgramRepository.countByRoadmapId(roadmapId)).willReturn(2);

        // when
        RoadmapResponse response = roadmapService.updateRoadmap(roadmapId, request, authorId);

        // then
        assertThat(response.title()).isEqualTo("수정된 제목");
        assertThat(response.description()).isEqualTo("수정된 설명");
        verify(roadmapProgramRepository, never()).deleteByRoadmapId(any());
    }

    @Test
    @DisplayName("로드맵 수정 - Safe Update: 프로그램 추가 (성공)")
    void updateRoadmap_SafeUpdate_AddPrograms_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "제목",
                "설명",
                Arrays.asList(1L, 2L, 3L), // 기존 [1, 2]에서 3 추가
                RoadmapStatus.PUBLISHED
        );

        Roadmap roadmap = Roadmap.create("제목", "설명", authorId, RoadmapStatus.PUBLISHED);
        ReflectionTestUtils.setField(roadmap, "enrolledStudents", 10); // 수강생 있음

        Program program1 = mock(Program.class);
        Program program2 = mock(Program.class);
        Program program3 = mock(Program.class);
        lenient().when(program1.getId()).thenReturn(1L);
        lenient().when(program2.getId()).thenReturn(2L);
        lenient().when(program3.getId()).thenReturn(3L);

        RoadmapProgram rp1 = RoadmapProgram.create(roadmap, program1, 0);
        RoadmapProgram rp2 = RoadmapProgram.create(roadmap, program2, 1);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));
        given(roadmapProgramRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmapId))
                .willReturn(Arrays.asList(rp1, rp2));
        given(programRepository.findById(1L)).willReturn(Optional.of(program1));
        given(programRepository.findById(2L)).willReturn(Optional.of(program2));
        given(programRepository.findById(3L)).willReturn(Optional.of(program3));
        given(userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(authorId, CourseRole.DESIGNER))
                .willReturn(true);
        given(roadmapProgramRepository.countByRoadmapId(roadmapId)).willReturn(3);

        // when
        RoadmapResponse response = roadmapService.updateRoadmap(roadmapId, request, authorId);

        // then
        assertThat(response.courseCount()).isEqualTo(3);
        verify(roadmapProgramRepository).deleteByRoadmapId(roadmapId);
        verify(roadmapProgramRepository, times(3)).save(any(RoadmapProgram.class));
    }

    @Test
    @DisplayName("로드맵 수정 - Destructive Update: 프로그램 삭제 (수강생 있음, 실패)")
    void updateRoadmap_DestructiveUpdate_RemoveProgram_WithEnrollments_Fail() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "제목",
                "설명",
                Arrays.asList(1L), // 기존 [1, 2]에서 2 삭제
                RoadmapStatus.PUBLISHED
        );

        Roadmap roadmap = Roadmap.create("제목", "설명", authorId, RoadmapStatus.PUBLISHED);
        ReflectionTestUtils.setField(roadmap, "enrolledStudents", 10); // 수강생 있음

        Program program1 = mock(Program.class);
        Program program2 = mock(Program.class);
        lenient().when(program1.getId()).thenReturn(1L);
        lenient().when(program2.getId()).thenReturn(2L);

        RoadmapProgram rp1 = RoadmapProgram.create(roadmap, program1, 0);
        RoadmapProgram rp2 = RoadmapProgram.create(roadmap, program2, 1);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));
        given(roadmapProgramRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmapId))
                .willReturn(Arrays.asList(rp1, rp2));
        given(programRepository.findById(1L)).willReturn(Optional.of(program1));
        given(userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(authorId, CourseRole.DESIGNER))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> roadmapService.updateRoadmap(roadmapId, request, authorId))
                .isInstanceOf(DestructiveUpdateNotAllowedException.class);

        verify(roadmapProgramRepository, never()).deleteByRoadmapId(any());
    }

    @Test
    @DisplayName("로드맵 수정 - Destructive Update: 순서 변경 (수강생 있음, 실패)")
    void updateRoadmap_DestructiveUpdate_ReorderPrograms_WithEnrollments_Fail() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "제목",
                "설명",
                Arrays.asList(2L, 1L), // 기존 [1, 2]에서 순서 변경
                RoadmapStatus.PUBLISHED
        );

        Roadmap roadmap = Roadmap.create("제목", "설명", authorId, RoadmapStatus.PUBLISHED);
        ReflectionTestUtils.setField(roadmap, "enrolledStudents", 10); // 수강생 있음

        Program program1 = mock(Program.class);
        Program program2 = mock(Program.class);
        lenient().when(program1.getId()).thenReturn(1L);
        lenient().when(program2.getId()).thenReturn(2L);

        RoadmapProgram rp1 = RoadmapProgram.create(roadmap, program1, 0);
        RoadmapProgram rp2 = RoadmapProgram.create(roadmap, program2, 1);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));
        given(roadmapProgramRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmapId))
                .willReturn(Arrays.asList(rp1, rp2));
        given(programRepository.findById(1L)).willReturn(Optional.of(program1));
        given(programRepository.findById(2L)).willReturn(Optional.of(program2));
        given(userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(authorId, CourseRole.DESIGNER))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> roadmapService.updateRoadmap(roadmapId, request, authorId))
                .isInstanceOf(DestructiveUpdateNotAllowedException.class);

        verify(roadmapProgramRepository, never()).deleteByRoadmapId(any());
    }

    @Test
    @DisplayName("로드맵 수정 - Destructive Update: 프로그램 삭제 (수강생 없음, 성공)")
    void updateRoadmap_DestructiveUpdate_RemoveProgram_NoEnrollments_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "제목",
                "설명",
                Arrays.asList(1L), // 기존 [1, 2]에서 2 삭제
                RoadmapStatus.PUBLISHED
        );

        Roadmap roadmap = Roadmap.create("제목", "설명", authorId, RoadmapStatus.PUBLISHED);
        ReflectionTestUtils.setField(roadmap, "enrolledStudents", 0); // 수강생 없음

        Program program1 = mock(Program.class);
        Program program2 = mock(Program.class);
        lenient().when(program1.getId()).thenReturn(1L);
        lenient().when(program2.getId()).thenReturn(2L);

        RoadmapProgram rp1 = RoadmapProgram.create(roadmap, program1, 0);
        RoadmapProgram rp2 = RoadmapProgram.create(roadmap, program2, 1);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));
        lenient().when(roadmapProgramRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmapId))
                .thenReturn(Arrays.asList(rp1, rp2));
        given(programRepository.findById(1L)).willReturn(Optional.of(program1));
        given(userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(authorId, CourseRole.DESIGNER))
                .willReturn(true);
        given(roadmapProgramRepository.countByRoadmapId(roadmapId)).willReturn(1);

        // when
        RoadmapResponse response = roadmapService.updateRoadmap(roadmapId, request, authorId);

        // then
        assertThat(response.courseCount()).isEqualTo(1);
        verify(roadmapProgramRepository).deleteByRoadmapId(roadmapId);
        verify(roadmapProgramRepository).save(any(RoadmapProgram.class));
    }

    @Test
    @DisplayName("로드맵 수정 - Destructive Update: DRAFT 상태에서는 항상 허용")
    void updateRoadmap_DestructiveUpdate_DraftStatus_Success() {
        // given
        Long roadmapId = 1L;
        Long authorId = 1L;
        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "제목",
                "설명",
                Arrays.asList(2L, 1L), // 순서 변경
                RoadmapStatus.DRAFT
        );

        Roadmap roadmap = Roadmap.create("제목", "설명", authorId, RoadmapStatus.DRAFT);
        ReflectionTestUtils.setField(roadmap, "enrolledStudents", 10); // 수강생이 있어도 DRAFT이면 허용

        Program program1 = mock(Program.class);
        Program program2 = mock(Program.class);
        lenient().when(program1.getId()).thenReturn(1L);
        lenient().when(program2.getId()).thenReturn(2L);

        RoadmapProgram rp1 = RoadmapProgram.create(roadmap, program1, 0);
        RoadmapProgram rp2 = RoadmapProgram.create(roadmap, program2, 1);

        given(roadmapRepository.findById(roadmapId)).willReturn(Optional.of(roadmap));
        lenient().when(roadmapProgramRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmapId))
                .thenReturn(Arrays.asList(rp1, rp2));
        given(programRepository.findById(1L)).willReturn(Optional.of(program1));
        given(programRepository.findById(2L)).willReturn(Optional.of(program2));
        given(userCourseRoleRepository.existsByUserIdAndCourseIdIsNullAndRole(authorId, CourseRole.DESIGNER))
                .willReturn(true);
        given(roadmapProgramRepository.countByRoadmapId(roadmapId)).willReturn(2);

        // when
        RoadmapResponse response = roadmapService.updateRoadmap(roadmapId, request, authorId);

        // then
        assertThat(response.courseCount()).isEqualTo(2);
        verify(roadmapProgramRepository).deleteByRoadmapId(roadmapId);
        verify(roadmapProgramRepository, times(2)).save(any(RoadmapProgram.class));
    }
}
