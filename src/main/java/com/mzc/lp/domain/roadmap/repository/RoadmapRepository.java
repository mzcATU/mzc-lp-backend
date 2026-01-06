package com.mzc.lp.domain.roadmap.repository;

import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import com.mzc.lp.domain.roadmap.entity.Roadmap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 로드맵 Repository
 */
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    /**
     * 작성자 ID로 로드맵 조회
     */
    Page<Roadmap> findByAuthorId(Long authorId, Pageable pageable);

    /**
     * 작성자 ID와 상태로 로드맵 조회
     */
    Page<Roadmap> findByAuthorIdAndStatus(Long authorId, RoadmapStatus status, Pageable pageable);

    /**
     * 작성자 ID로 로드맵 조회 (최신순 정렬)
     */
    Page<Roadmap> findByAuthorIdOrderByUpdatedAtDesc(Long authorId, Pageable pageable);

    /**
     * 작성자 ID와 상태로 로드맵 조회 (최신순 정렬)
     */
    Page<Roadmap> findByAuthorIdAndStatusOrderByUpdatedAtDesc(Long authorId, RoadmapStatus status, Pageable pageable);

    /**
     * 작성자 ID로 로드맵 조회 (수강생순 정렬)
     */
    Page<Roadmap> findByAuthorIdOrderByEnrolledStudentsDesc(Long authorId, Pageable pageable);

    /**
     * 작성자 ID와 상태로 로드맵 조회 (수강생순 정렬)
     */
    Page<Roadmap> findByAuthorIdAndStatusOrderByEnrolledStudentsDesc(Long authorId, RoadmapStatus status, Pageable pageable);

    /**
     * 작성자 ID로 로드맵 조회 (제목순 정렬)
     */
    Page<Roadmap> findByAuthorIdOrderByTitleAsc(Long authorId, Pageable pageable);

    /**
     * 작성자 ID와 상태로 로드맵 조회 (제목순 정렬)
     */
    Page<Roadmap> findByAuthorIdAndStatusOrderByTitleAsc(Long authorId, RoadmapStatus status, Pageable pageable);

    /**
     * 작성자의 전체 로드맵 개수
     */
    long countByAuthorId(Long authorId);

    /**
     * 작성자의 상태별 로드맵 개수
     */
    long countByAuthorIdAndStatus(Long authorId, RoadmapStatus status);

    /**
     * 작성자의 총 수강생 수 (모든 로드맵의 합)
     */
    @Query("SELECT COALESCE(SUM(r.enrolledStudents), 0) FROM Roadmap r WHERE r.authorId = :authorId")
    long sumEnrolledStudentsByAuthorId(@Param("authorId") Long authorId);

    /**
     * 작성자의 평균 강의 수
     */
    @Query("SELECT AVG(SIZE(r.id)) FROM Roadmap r " +
           "LEFT JOIN RoadmapProgram rp ON rp.roadmap.id = r.id " +
           "WHERE r.authorId = :authorId " +
           "GROUP BY r.id")
    Double getAverageCourseCountByAuthorId(@Param("authorId") Long authorId);
}
