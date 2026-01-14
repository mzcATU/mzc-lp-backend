package com.mzc.lp.domain.roadmap.repository;

import com.mzc.lp.domain.roadmap.entity.RoadmapProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 로드맵-프로그램 Repository
 */
public interface RoadmapProgramRepository extends JpaRepository<RoadmapProgram, Long> {

    /**
     * 로드맵 ID로 프로그램 목록 조회 (순서대로)
     */
    List<RoadmapProgram> findByRoadmapIdOrderByOrderIndexAsc(Long roadmapId);

    /**
     * 로드맵 ID로 모든 프로그램 삭제
     */
    @Modifying
    @Query("DELETE FROM RoadmapProgram rp WHERE rp.roadmap.id = :roadmapId")
    void deleteByRoadmapId(@Param("roadmapId") Long roadmapId);

    /**
     * 로드맵에 프로그램이 이미 포함되어 있는지 확인
     */
    boolean existsByRoadmapIdAndProgramId(Long roadmapId, Long programId);

    /**
     * 로드맵의 프로그램 개수
     */
    @Query("SELECT COUNT(rp) FROM RoadmapProgram rp WHERE rp.roadmap.id = :roadmapId")
    int countByRoadmapId(@Param("roadmapId") Long roadmapId);

}
