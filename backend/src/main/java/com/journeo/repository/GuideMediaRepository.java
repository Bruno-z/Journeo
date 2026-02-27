package com.journeo.repository;

import com.journeo.model.GuideMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuideMediaRepository extends JpaRepository<GuideMedia, Long> {

    List<GuideMedia> findByGuideIdOrderByUploadedAtDesc(Long guideId);
}
