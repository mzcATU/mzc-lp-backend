package com.mzc.lp.domain.content.repository;

import com.mzc.lp.domain.content.entity.ContentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentVersionRepository extends JpaRepository<ContentVersion, Long> {

    List<ContentVersion> findByContentIdOrderByVersionNumberDesc(Long contentId);

    Optional<ContentVersion> findByContentIdAndVersionNumber(Long contentId, Integer versionNumber);

    @Query("SELECT MAX(v.versionNumber) FROM ContentVersion v WHERE v.content.id = :contentId")
    Optional<Integer> findMaxVersionNumber(@Param("contentId") Long contentId);

    @Modifying
    @Query("DELETE FROM ContentVersion v WHERE v.content.id = :contentId")
    void deleteByContentId(@Param("contentId") Long contentId);
}
