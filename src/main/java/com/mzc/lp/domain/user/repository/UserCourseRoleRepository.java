package com.mzc.lp.domain.user.repository;

import com.mzc.lp.domain.user.constant.CourseRole;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCourseRoleRepository extends JpaRepository<UserCourseRole, Long> {

    List<UserCourseRole> findByUserId(Long userId);

    Optional<UserCourseRole> findByUserIdAndCourseIdIsNullAndRole(Long userId, CourseRole role);

    boolean existsByUserIdAndCourseIdIsNullAndRole(Long userId, CourseRole role);

    boolean existsByUserIdAndCourseIdAndRole(Long userId, Long courseId, CourseRole role);

    List<UserCourseRole> findByUserIdAndCourseId(Long userId, Long courseId);

    List<UserCourseRole> findByCourseIdAndRole(Long courseId, CourseRole role);
}
