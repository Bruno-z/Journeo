package com.journeo.repository;

import com.journeo.model.Guide;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {

    List<Guide> findByUsersId(Long userId);

    Page<Guide> findAll(Pageable pageable);

    Page<Guide> findByUsersId(Long userId, Pageable pageable);
}