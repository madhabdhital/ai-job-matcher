package com.jobmatcher.service;

import com.jobmatcher.repository.JobListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class JobSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobSyncScheduler.class);

    private final FreeJobSourceService freeJobSourceService;
    private final IndianApiJobService indianApiJobService;
    private final JobListingRepository jobListingRepository;

    public JobSyncScheduler(FreeJobSourceService freeJobSourceService,
                            IndianApiJobService indianApiJobService,
                            JobListingRepository jobListingRepository) {
        this.freeJobSourceService = freeJobSourceService;
        this.indianApiJobService = indianApiJobService;
        this.jobListingRepository = jobListingRepository;
    }

    // First run: fill an empty database so the homepage is not blank.
    @EventListener(ApplicationReadyEvent.class)
    public void importOnStartupIfEmpty() {
        if (jobListingRepository.count() == 0) {
            syncAll();
        }
    }

    // Every day at 3:00 AM server time.
    @Scheduled(cron = "0 0 3 * * *")
    public void importDaily() {
        syncAll();
    }

    private void syncAll() {
        try {
            int saved = freeJobSourceService.syncHimalayas();
            log.info("Free job source imported {} new jobs", saved);
        } catch (Exception e) {
            log.warn("Free job source import failed ({})", e.getClass().getSimpleName());
        }

        try {
            int saved = indianApiJobService.fetchAndSaveJobs();
            log.info("IndianAPI imported {} new jobs", saved);
        } catch (Exception e) {
            log.warn("IndianAPI import failed ({})", e.getClass().getSimpleName());
        }
    }
}