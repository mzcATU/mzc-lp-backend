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
     * 사용자의 CourseRole 목록을 Program title과 함께 조회
     * courseId는 실제로 Program ID를 저장하므로 Program 테이블과 조인
     */
    @Query("SELECT ucr, p.title FROM UserCourseRole ucr " +
           "LEFT JOIN Program p ON ucr.courseId = p.id " +
           "WHERE ucr.user.id = :userId")
    List<Object[]> findByUserIdWithProgramTitle(@Param("userId") Long userId);

    Optional<UserCourseRole> findByUserIdAndCourseIdIsNullAndRole(Long userId, CourseRole role);

    boolean existsByUserIdAndCourseIdIsNullAndRole(Long userId, CourseRole role);

    boolean existsByUserIdAndCourseIdAndRole(Long userId, Long courseId, CourseRole role);

    List<UserCourseRole> findByUserIdAndCourseId(Long userId, Long courseId);

    List<UserCourseRole> findByCourseIdAndRole(Long courseId, CourseRole role);
}
