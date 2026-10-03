package com.jobmatcher.dto;

import java.util.List;

public record PagedJobsResponse(
        List<JobResponse> jobs,
        int page,
        int totalPages,
        long totalElements) {
}