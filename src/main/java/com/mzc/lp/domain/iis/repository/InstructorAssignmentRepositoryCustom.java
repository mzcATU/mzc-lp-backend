package com.mzc.lp.domain.iis.repository;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InstructorAssignmentRepositoryCustom {

    Page<InstructorAssignment> searchAssignments(
            Long tenantId,
            Long instructorId,
            Long courseTimeId,
            InstructorRole role,
            AssignmentStatus status,
            Pageable pageable
    );
}
