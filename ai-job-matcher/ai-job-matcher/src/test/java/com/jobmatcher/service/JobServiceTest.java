package com.jobmatcher.service;

import com.jobmatcher.dto.JobResponse;
import com.jobmatcher.dto.PagedJobsResponse;
import com.jobmatcher.entity.JobListing;
import com.jobmatcher.entity.MasterResume;
import com.jobmatcher.entity.User;
import com.jobmatcher.repository.JobListingRepository;
import com.jobmatcher.repository.MasterResumeRepository;
import com.jobmatcher.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock private JobListingRepository jobListingRepository;
    @Mock private MasterResumeRepository masterResumeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private JobService jobService;

    private JobListing job(String title, String company) {
        JobListing job = new JobListing();
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation("Remote");
        job.setJobType(JobListing.JobType.INTERNSHIP);
        job.setWorkMode(JobListing.WorkMode.REMOTE);
        job.setDomain("Software Development");
        job.setDescription("Build things with Java and React.");
        job.setSource("Himalayas");
        return job;
    }

    private Specification<JobListing> anySpec() {
        return ArgumentMatchers.<Specification<JobListing>>any();
    }

    // ---------- search ----------

    @Test
    void searchJobs_mapsResultsAndPagingInfo() {
        Page<JobListing> page = new PageImpl<>(
                List.of(job("Java Intern", "Acme")), PageRequest.of(0, 12), 25);
        when(jobListingRepository.findAll(anySpec(), any(Pageable.class))).thenReturn(page);

        PagedJobsResponse response = jobService.searchJobs("java", null, null, 0, 12);

        assertEquals(1, response.jobs().size());
        assertEquals("Java Intern", response.jobs().get(0).getTitle());
        assertEquals(0, response.page());
        assertEquals(3, response.totalPages());     // 25 results / 12 per page
        assertEquals(25, response.totalElements());
    }

    @Test
    void searchJobs_listResultsDoNotIncludeFullDescription() {
        Page<JobListing> page = new PageImpl<>(List.of(job("Java Intern", "Acme")));
        when(jobListingRepository.findAll(anySpec(), any(Pageable.class))).thenReturn(page);

        PagedJobsResponse response = jobService.searchJobs(null, null, null, 0, 12);

        assertNull(response.jobs().get(0).getDescription());
    }

    @Test
    void searchJobs_clampsNegativePageAndOversizedPageSize() {
        when(jobListingRepository.findAll(anySpec(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        jobService.searchJobs(null, null, null, -5, 500);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobListingRepository).findAll(anySpec(), captor.capture());

        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(50, captor.getValue().getPageSize());   // MAX_PAGE_SIZE
    }

    @Test
    void searchJobs_zeroPageSizeBecomesOne() {
        when(jobListingRepository.findAll(anySpec(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        jobService.searchJobs(null, null, null, 0, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobListingRepository).findAll(anySpec(), captor.capture());

        assertEquals(1, captor.getValue().getPageSize());
    }

    @Test
    void searchJobs_jobWithoutDomainStillGetsADomainInResponse() {
        JobListing noDomain = job("Backend Developer", "Acme");
        noDomain.setDomain(null);
        when(jobListingRepository.findAll(anySpec(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(noDomain)));

        PagedJobsResponse response = jobService.searchJobs(null, null, null, 0, 12);

        String domain = response.jobs().get(0).getDomain();
        assertNotNull(domain);
        assertFalse(domain.isBlank());
    }

    // ---------- job details ----------

    @Test
    void getJobById_includesDescription() {
        when(jobListingRepository.findById(1L)).thenReturn(Optional.of(job("Java Intern", "Acme")));

        JobResponse response = jobService.getJobById(1L);

        assertEquals("Java Intern", response.getTitle());
        assertEquals("Build things with Java and React.", response.getDescription());
    }

    @Test
    void getJobById_unknownId_throws() {
        when(jobListingRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () -> jobService.getJobById(99L));
        assertEquals("Job not found", error.getMessage());
    }

    // ---------- recommendations ----------

    @Test
    void getRecommendedJobs_withoutResume_asksToUploadOne() {
        User user = new User("Test User", "test@example.com", "hashed");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(masterResumeRepository.findByUserId(1L)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () ->
                jobService.getRecommendedJobs("test@example.com", null, null, 0, 12));

        assertEquals("Please upload your resume first.", error.getMessage());
    }

    @Test
    void getRecommendedJobs_unknownUser_throws() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                jobService.getRecommendedJobs("ghost@example.com", null, null, 0, 12));
    }

    @Test
    void getRecommendedJobs_withResume_returnsJobs() {
        User user = new User("Test User", "test@example.com", "hashed");
        ReflectionTestUtils.setField(user, "id", 1L);

        MasterResume resume = new MasterResume();
        resume.setSpecialization("Software Development");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(masterResumeRepository.findByUserId(1L)).thenReturn(Optional.of(resume));
        when(jobListingRepository.findAll(anySpec(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(job("Java Intern", "Acme"))));

        PagedJobsResponse response =
                jobService.getRecommendedJobs("test@example.com", null, null, 0, 12);

        assertEquals(1, response.jobs().size());
    }

    @Test
    void getRecommendedJobs_generalDomain_stillReturnsJobs() {
        User user = new User("Test User", "test@example.com", "hashed");
        ReflectionTestUtils.setField(user, "id", 1L);

        MasterResume resume = new MasterResume();
        resume.setSpecialization("General");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(masterResumeRepository.findByUserId(1L)).thenReturn(Optional.of(resume));
        when(jobListingRepository.findAll(anySpec(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(job("Any Job", "Acme"))));

        assertEquals(1,
                jobService.getRecommendedJobs("test@example.com", null, null, 0, 12).jobs().size());
    }
}