package com.jobmatcher.repository;

import com.jobmatcher.entity.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, Long> {
    List<JobAnalysis> findByUserId(Long userId);
    Optional<JobAnalysis> findByUserIdAndJobListingId(Long userId, Long jobListingId);
}