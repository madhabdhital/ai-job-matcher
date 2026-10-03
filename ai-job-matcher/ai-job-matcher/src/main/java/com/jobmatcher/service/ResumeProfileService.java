package com.jobmatcher.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeProfileService {

    private static final Map<String, List<String>> DOMAIN_KEYWORDS = Map.of(
            "Software Development", List.of(
                    "java", "spring", "spring boot", "python", "javascript",
                    "typescript", "react", "node", "node.js", "next.js",
                    "c++", "c#", ".net", "software developer", "software engineer",
                    "backend", "frontend", "full stack", "web development"
            ),
            "Data Science & AI", List.of(
                    "machine learning", "deep learning", "data science", "data scientist",
                    "artificial intelligence", "tensorflow", "pytorch", "scikit-learn",
                    "pandas", "numpy", "nlp", "natural language processing"
            ),
            "Data & Analytics", List.of(
                    "sql", "mysql", "postgresql", "power bi", "tableau",
                    "data analyst", "business intelligence", "excel", "analytics"
            ),
            "Cloud & DevOps", List.of(
                    "aws", "azure", "gcp", "docker", "kubernetes", "terraform",
                    "devops", "ci/cd", "jenkins", "cloud engineer"
            ),
            "Cybersecurity", List.of(
                    "cybersecurity", "cyber security", "penetration testing",
                    "ethical hacking", "siem", "soc", "network security",
                    "information security", "cryptography"
            ),
            "UI/UX & Design", List.of(
                    "ui/ux", "ui ux", "figma", "adobe xd", "user experience",
                    "user interface", "graphic design", "product design"
            ),
            "Marketing & Sales", List.of(
                    "digital marketing", "seo", "sem", "social media marketing",
                    "content marketing", "sales", "business development"
            ),
            "Finance & Accounting", List.of(
                    "accounting", "finance", "financial analysis", "tally",
                    "audit", "taxation", "investment"
            ),
            "Human Resources", List.of(
                    "human resources", "hr", "recruitment", "talent acquisition",
                    "employee relations"
            )
    );

    public ResumeProfile analyze(String text) {
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT);
        Map<String, Integer> domainScores = new HashMap<>();
        Set<String> skills = new LinkedHashSet<>();

        for (Map.Entry<String, List<String>> entry : DOMAIN_KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (normalized.contains(keyword)) {
                    score++;
                    skills.add(keyword);
                }
            }
            domainScores.put(entry.getKey(), score);
        }

        String domain = domainScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse("General");

        return new ResumeProfile(domain, new ArrayList<>(skills));
    }

    public record ResumeProfile(String specialization, List<String> skills) {
        public String skillsJson() {
            return skills.stream()
                    .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
                    .collect(Collectors.joining(",", "[", "]"));
        }

        public String specializationJson() {
            return "\"" + specialization.replace("\"", "\\\"") + "\"";
        }
    }
}
