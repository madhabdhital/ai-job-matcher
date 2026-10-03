package com.jobmatcher.service;

import com.jobmatcher.entity.JobListing;

import java.util.Locale;
import java.util.Map;
import java.util.List;

public final class DomainMatcher {
    private DomainMatcher() {}

    private static final Map<String, List<String>> DOMAIN_KEYWORDS = Map.of(
            "Software Development", List.of("java","spring","python","javascript","typescript","react","node","next.js","software developer","software engineer","backend","frontend","full stack",".net"),
            "Data Science & AI", List.of("machine learning","deep learning","data science","data scientist","artificial intelligence","tensorflow","pytorch","nlp"),
            "Data & Analytics", List.of("sql","power bi","tableau","data analyst","analytics","business intelligence","excel"),
            "Cloud & DevOps", List.of("aws","azure","gcp","docker","kubernetes","terraform","devops","jenkins","cloud"),
            "Cybersecurity", List.of("cybersecurity","cyber security","penetration testing","ethical hacking","siem","soc","information security"),
            "UI/UX & Design", List.of("ui/ux","ui ux","figma","adobe xd","user experience","user interface","graphic design"),
            "Marketing & Sales", List.of("digital marketing","seo","social media marketing","sales","business development"),
            "Finance & Accounting", List.of("accounting","finance","financial analysis","tally","audit","taxation"),
            "Human Resources", List.of("human resources","recruitment","talent acquisition")
    );

    public static String detect(String title, String description) {
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description))
                .toLowerCase(Locale.ROOT);
        return DOMAIN_KEYWORDS.entrySet().stream()
                .max((a,b) -> Integer.compare(score(a.getValue(), text), score(b.getValue(), text)))
                .filter(e -> score(e.getValue(), text) > 0)
                .map(Map.Entry::getKey)
                .orElse("General");
    }

    public static boolean matches(String domain, JobListing job) {
        if (domain == null || domain.isBlank() || "General".equalsIgnoreCase(domain)) return true;
        return domain.equalsIgnoreCase(detect(job.getTitle(), job.getDescription()));
    }

    private static int score(List<String> keywords, String text) {
        int score = 0;
        for (String keyword : keywords) if (text.contains(keyword)) score++;
        return score;
    }
}
