package com.mzc.lp.domain.user.repository;

import com.mzc.lp.domain.user.constant.CourseRole;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCourseRoleRepository extends JpaRepository<UserCourseRole, Long> {

    List<UserCourseRole> findByUserId(Long userId);

    /**
     * 사용자의 CourseRole 목록을 Course title과 함께 조회
     * courseId는 Course ID를 저장하므로 Course 테이블과 조인
     */
    @Query("SELECT ucr, c.title FROM UserCourseRole ucr " +
           "LEFT JOIN Course c ON ucr.courseId = c.id " +
           "WHERE ucr.user.id = :userId")
    List<Object[]> findByUserIdWithCourseTitle(@Param("userId") Long userId);

    Optional<UserCourseRole> findByUserIdAndCourseIdIsNullAndRole(Long userId, CourseRole role);

    boolean existsByUserIdAndCourseIdIsNullAndRole(Long userId, CourseRole role);

    boolean existsByUserIdAndCourseIdAndRole(Long userId, Long courseId, CourseRole role);

    List<UserCourseRole> findByUserIdAndCourseId(Long userId, Long courseId);

    List<UserCourseRole> findByCourseIdAndRole(Long courseId, CourseRole role);
}
