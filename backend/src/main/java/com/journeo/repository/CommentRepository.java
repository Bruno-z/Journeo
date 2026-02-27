package com.journeo.repository;

import com.journeo.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByGuideIdOrderByCreatedAtDesc(Long guideId);

    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.guide.id = :guideId")
    Double findAverageRatingByGuideId(@Param("guideId") Long guideId);
}
