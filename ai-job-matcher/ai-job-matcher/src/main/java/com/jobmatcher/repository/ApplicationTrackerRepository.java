
package com.jobmatcher.repository;

import com.jobmatcher.entity.ApplicationTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationTrackerRepository
        extends JpaRepository<ApplicationTracker, Long> {

    List<ApplicationTracker> findByUserId(Long userId);

    Optional<ApplicationTracker> findByUserIdAndJobId(
            Long userId,
            Long jobId
    );

    boolean existsByUserIdAndJobId(
            Long userId,
            Long jobId
    );
}

