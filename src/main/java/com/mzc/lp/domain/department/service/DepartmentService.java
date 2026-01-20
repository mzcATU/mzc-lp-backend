package com.mzc.lp.domain.department.service;

import com.mzc.lp.domain.department.dto.request.CreateDepartmentRequest;
import com.mzc.lp.domain.department.dto.request.UpdateDepartmentRequest;
import com.mzc.lp.domain.department.dto.response.DepartmentMemberResponse;
import com.mzc.lp.domain.department.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    /**
     * 부서 생성
     */
    DepartmentResponse create(Long tenantId, CreateDepartmentRequest request);

    /**
     * 부서 수정
     */
    DepartmentResponse update(Long tenantId, Long departmentId, UpdateDepartmentRequest request);

    /**
     * 부서 삭제
     */
    void delete(Long tenantId, Long departmentId);

    /**
     * 부서 상세 조회
     */
    DepartmentResponse getById(Long tenantId, Long departmentId);

    /**
     * 전체 부서 목록 조회 (계층 구조)
     */
    List<DepartmentResponse> getAll(Long tenantId);

    /**
     * 최상위 부서 목록 조회 (하위 포함)
     */
    List<DepartmentResponse> getRootDepartments(Long tenantId);

    /**
     * 부서 검색
     */
    List<DepartmentResponse> search(Long tenantId, String keyword);

    /**
     * 활성 부서 목록 조회
     */
    List<DepartmentResponse> getActiveDepartments(Long tenantId);

    /**
     * 부서별 소속 인원 목록 조회
     */
    List<DepartmentMemberResponse> getMembersByDepartmentId(Long tenantId, Long departmentId);

    /**
     * 부서에 추가 가능한 인원 목록 조회
     * (해당 부서에 소속되지 않은 활성 사용자)
     */
    List<DepartmentMemberResponse> getAvailableMembersForDepartment(Long tenantId, Long departmentId);

    /**
     * 부서에 인원 추가 (사용자의 부서를 변경)
     */
    void addMemberToDepartment(Long tenantId, Long departmentId, Long userId);

    /**
     * 부서명으로 부서 조회 또는 자동 생성
     * 부서명이 존재하면 해당 부서 반환, 없으면 자동 생성 후 반환
     */
    DepartmentResponse getOrCreateByName(Long tenantId, String departmentName);
}
