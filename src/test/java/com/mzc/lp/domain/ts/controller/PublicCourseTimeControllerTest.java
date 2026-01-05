package com.mzc.lp.domain.ts.controller;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PublicCourseTimeControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private UserCourseRoleRepository userCourseRoleRepository;

    @Autowired
    private InstructorAssignmentRepository instructorAssignmentRepository;

    private Program testProgram;

    @BeforeEach
    void setUp() {
        instructorAssignmentRepository.deleteAll();
        courseTimeRepository.deleteAll();
        userCourseRoleRepository.deleteAll();
        programRepository.deleteAll();

        testProgram = createApprovedProgram();
    }

    private Program createApprovedProgram() {
        Program program = Program.create("테스트 프로그램", 1L);
        program.submit();
        program.approve(1L, "테스트 승인");
        return programRepository.save(program);
    }

    private CourseTime createCourseTimeWithStatus(String title, CourseTimeStatus status) {
        CourseTime courseTime = CourseTime.create(
                title,
                DeliveryType.ONLINE,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(30),
                30,
                5,
                EnrollmentMethod.FIRST_COME,
                80,
                new BigDecimal("100000"),
                false,
                null,
                true,
                1L
        );
        courseTime.linkProgram(testProgram);

        // 상태 전이
        if (status == CourseTimeStatus.RECRUITING || status == CourseTimeStatus.ONGOING) {
            courseTime.open();
        }
        if (status == CourseTimeStatus.ONGOING) {
            courseTime.startClass();
        }

        return courseTimeRepository.save(courseTime);
    }

    private CourseTime createOnDemandCourseTime(String title) {
        CourseTime courseTime = CourseTime.create(
                title,
                DeliveryType.ONLINE,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(9999, 12, 31),
                LocalDate.of(2020, 1, 1),
                LocalDate.of(9999, 12, 31),
                null,  // 무제한 정원
                null,
                EnrollmentMethod.FIRST_COME,
                80,
                BigDecimal.ZERO,
                true,
                null,
                true,
                1L
        );
        courseTime.linkProgram(testProgram);
        courseTime.open();

        return courseTimeRepository.save(courseTime);
    }

    @Nested
    @DisplayName("GET /api/public/course-times - 목록 조회")
    class GetPublicCourseTimes {

        @Test
        @DisplayName("인증 없이 조회 성공")
        void getPublicCourseTimes_withoutAuth_success() throws Exception {
            // given
            createCourseTimeWithStatus("모집 중 과정", CourseTimeStatus.RECRUITING);
            createCourseTimeWithStatus("진행 중 과정", CourseTimeStatus.ONGOING);

            // when & then
            mockMvc.perform(get("/api/public/course-times")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(2)));
        }

        @Test
        @DisplayName("DRAFT 상태 차수는 목록에서 제외")
        void getPublicCourseTimes_excludeDraft() throws Exception {
            // given
            createCourseTimeWithStatus("모집 중 과정", CourseTimeStatus.RECRUITING);
            createCourseTimeWithStatus("초안 과정", CourseTimeStatus.DRAFT);

            // when & then
            mockMvc.perform(get("/api/public/course-times")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].title").value("모집 중 과정"));
        }

        @Test
        @DisplayName("status 파라미터로 필터링")
        void getPublicCourseTimes_filterByStatus() throws Exception {
            // given
            createCourseTimeWithStatus("모집 중 과정", CourseTimeStatus.RECRUITING);
            createCourseTimeWithStatus("진행 중 과정", CourseTimeStatus.ONGOING);

            // when & then
            mockMvc.perform(get("/api/public/course-times")
                            .param("status", "RECRUITING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].status").value("RECRUITING"));
        }

        @Test
        @DisplayName("isFree 파라미터로 필터링")
        void getPublicCourseTimes_filterByFree() throws Exception {
            // given
            createCourseTimeWithStatus("유료 과정", CourseTimeStatus.RECRUITING);
            createOnDemandCourseTime("무료 과정");

            // when & then
            mockMvc.perform(get("/api/public/course-times")
                            .param("isFree", "true")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].isFree").value(true));
        }

        @Test
        @DisplayName("keyword 파라미터로 제목 검색")
        void getPublicCourseTimes_searchByKeyword() throws Exception {
            // given
            createCourseTimeWithStatus("Python 기초 과정", CourseTimeStatus.RECRUITING);
            createCourseTimeWithStatus("Java 고급 과정", CourseTimeStatus.RECRUITING);

            // when & then
            mockMvc.perform(get("/api/public/course-times")
                            .param("keyword", "python")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].title", containsStringIgnoringCase("python")));
        }

        @Test
        @DisplayName("상시모집 차수 isOnDemand 필드 검증")
        void getPublicCourseTimes_onDemandFlag() throws Exception {
            // given
            createOnDemandCourseTime("상시모집 과정");

            // when & then
            mockMvc.perform(get("/api/public/course-times")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].isOnDemand").value(true));
        }

        @Test
        @DisplayName("페이징 처리")
        void getPublicCourseTimes_pagination() throws Exception {
            // given
            for (int i = 0; i < 25; i++) {
                createCourseTimeWithStatus("과정 " + i, CourseTimeStatus.RECRUITING);
            }

            // when & then
            mockMvc.perform(get("/api/public/course-times")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(10)))
                    .andExpect(jsonPath("$.data.totalElements").value(25))
                    .andExpect(jsonPath("$.data.totalPages").value(3));
        }
    }

    @Nested
    @DisplayName("GET /api/public/course-times/{id} - 상세 조회")
    class GetPublicCourseTime {

        @Test
        @DisplayName("인증 없이 상세 조회 성공")
        void getPublicCourseTime_withoutAuth_success() throws Exception {
            // given
            CourseTime courseTime = createCourseTimeWithStatus("모집 중 과정", CourseTimeStatus.RECRUITING);

            // when & then
            mockMvc.perform(get("/api/public/course-times/{id}", courseTime.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(courseTime.getId()))
                    .andExpect(jsonPath("$.data.title").value("모집 중 과정"))
                    .andExpect(jsonPath("$.data.program").exists())
                    .andExpect(jsonPath("$.data.curriculum").isArray())
                    .andExpect(jsonPath("$.data.instructors").isArray());
        }

        @Test
        @DisplayName("DRAFT 상태 차수 조회 시 404")
        void getPublicCourseTime_draftStatus_notFound() throws Exception {
            // given
            CourseTime courseTime = createCourseTimeWithStatus("초안 과정", CourseTimeStatus.DRAFT);

            // when & then
            mockMvc.perform(get("/api/public/course-times/{id}", courseTime.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 404")
        void getPublicCourseTime_notFound() throws Exception {
            // when & then
            mockMvc.perform(get("/api/public/course-times/{id}", 99999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("상세 응답에 enrollmentMethod 포함")
        void getPublicCourseTime_containsEnrollmentMethod() throws Exception {
            // given
            CourseTime courseTime = createCourseTimeWithStatus("모집 중 과정", CourseTimeStatus.RECRUITING);

            // when & then
            mockMvc.perform(get("/api/public/course-times/{id}", courseTime.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.enrollmentMethod").value("FIRST_COME"))
                    .andExpect(jsonPath("$.data.minProgressForCompletion").value(80))
                    .andExpect(jsonPath("$.data.allowLateEnrollment").value(true));
        }
    }
}
