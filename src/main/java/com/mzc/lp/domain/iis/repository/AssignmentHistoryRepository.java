package com.mzc.lp.domain.iis.repository;

import com.mzc.lp.domain.iis.entity.AssignmentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {

    List<AssignmentHistory> findByAssignmentIdOrderByChangedAtDesc(Long assignmentId);

    Page<AssignmentHistory> findByAssignmentId(Long assignmentId, Pageable pageable);
}
