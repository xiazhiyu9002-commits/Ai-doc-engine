package com.aidoc.engine.repository;

import com.aidoc.engine.model.entity.AnnouncementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {
    
    Page<AnnouncementEntity> findByIsPublishedTrue(Pageable pageable);
    
    Page<AnnouncementEntity> findByAnnouncementType(String announcementType, Pageable pageable);
    
    Page<AnnouncementEntity> findByCreatedBy(Long createdBy, Pageable pageable);
    
    List<AnnouncementEntity> findByIsPublishedTrueOrderByPublishedAtDesc();
    
    @Query("SELECT a FROM AnnouncementEntity a WHERE a.isPublished = true AND " +
           "(a.expireAt IS NULL OR a.expireAt > :now) " +
           "ORDER BY a.publishedAt DESC")
    List<AnnouncementEntity> findPublishedAndNotExpired(@Param("now") LocalDateTime now);
    
    @Query(value = "SELECT * FROM sys_announcement a WHERE " +
           "(CAST(:announcementType AS VARCHAR) IS NULL OR a.announcement_type = :announcementType) AND " +
           "(CAST(:isPublished AS BOOLEAN) IS NULL OR a.is_published = :isPublished)",
           nativeQuery = true)
    Page<AnnouncementEntity> findByConditions(
            @Param("announcementType") String announcementType,
            @Param("isPublished") Boolean isPublished,
            Pageable pageable);
}
