package com.aidoc.engine.service;

import com.aidoc.engine.model.dto.announcement.AnnouncementCreateRequest;
import com.aidoc.engine.model.vo.admin.PageResult;
import com.aidoc.engine.model.vo.announcement.AnnouncementVO;

import java.util.List;

public interface AnnouncementService {
    
    AnnouncementVO createAnnouncement(AnnouncementCreateRequest request, Long userId);
    
    AnnouncementVO updateAnnouncement(Long id, AnnouncementCreateRequest request);
    
    void deleteAnnouncement(Long id);
    
    AnnouncementVO publishAnnouncement(Long id);
    
    AnnouncementVO unpublishAnnouncement(Long id);
    
    PageResult<AnnouncementVO> getAnnouncementList(int page, int size, String type, Boolean isPublished);
    
    List<AnnouncementVO> getPublishedAnnouncements();
    
    AnnouncementVO getAnnouncementById(Long id);
}
