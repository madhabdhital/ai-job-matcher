package com.jobmatcher.controller;

import com.jobmatcher.entity.MasterResume;
import com.jobmatcher.entity.User;
import com.jobmatcher.repository.MasterResumeRepository;
import com.jobmatcher.repository.UserRepository;
import com.jobmatcher.service.LlmService;
import com.jobmatcher.service.ResumeParserService;
import com.jobmatcher.service.ResumeProfileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {
    private final ResumeParserService resumeParserService;
    private final LlmService llmService;
    private final MasterResumeRepository masterResumeRepository;
    private final UserRepository userRepository;
    private final ResumeProfileService resumeProfileService;

    public ResumeController(ResumeParserService resumeParserService,
                            LlmService llmService,
                            MasterResumeRepository masterResumeRepository,
                            UserRepository userRepository,
                            ResumeProfileService resumeProfileService) {
        this.resumeParserService = resumeParserService;
        this.llmService = llmService;
        this.masterResumeRepository = masterResumeRepository;
        this.userRepository = userRepository;
        this.resumeProfileService = resumeProfileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMasterResume(@RequestParam("file") MultipartFile file,
                                                Authentication authentication) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a file");
        }

        User user = getUser(authentication.getName());
        String parsedText = resumeParserService.extractText(file);
        String extension = getExtension(file.getOriginalFilename());
        ResumeProfileService.ResumeProfile profile = resumeProfileService.analyze(parsedText);

        MasterResume masterResume = masterResumeRepository.findByUserId(user.getId())
                .orElse(new MasterResume());

        masterResume.setUser(user);
        masterResume.setFilePath(UUID.randomUUID() + extension);
        masterResume.setOriginalFilename(file.getOriginalFilename());
        masterResume.setContentType(file.getContentType());
        try {
            masterResume.setFileData(file.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Could not store resume file", e);
        }
        masterResume.setExtractedText(parsedText);
        masterResume.setSpecialization(profile.specializationJson());
        masterResume.setExtractedSkills(profile.skillsJson());
        masterResumeRepository.save(masterResume);

        return ResponseEntity.ok(Map.of(
                "message", "Resume uploaded and parsed successfully",
                "filename", masterResume.getOriginalFilename(),
                "specialization", profile.specialization(),
                "skills", profile.skills(),
                "extractedLength", parsedText.length()
        ));
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentResume(Authentication authentication) {
        User user = getUser(authentication.getName());
        MasterResume resume = masterResumeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No resume uploaded yet."));

        return ResponseEntity.ok(Map.of(
                "filename", resume.getOriginalFilename() == null ? "Resume" : resume.getOriginalFilename(),
                "contentType", resume.getContentType() == null ? "application/pdf" : resume.getContentType(),
                "specialization", resume.getSpecialization() == null ? "\"General\"" : resume.getSpecialization(),
                "extractedSkills", resume.getExtractedSkills() == null ? "[]" : resume.getExtractedSkills(),
                "extractedText", resume.getExtractedText() == null ? "" : resume.getExtractedText(),
                "uploadedAt", resume.getUploadedAt()
        ));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadResume(Authentication authentication) {
        User user = getUser(authentication.getName());
        MasterResume resume = masterResumeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No resume uploaded yet."));

        if (resume.getFileData() == null || resume.getFileData().length == 0) {
            throw new RuntimeException("The uploaded resume file is not available. Please upload it again.");
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (resume.getContentType() != null) mediaType = MediaType.parseMediaType(resume.getContentType());
        } catch (Exception ignored) {}

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + (resume.getOriginalFilename() == null ? "resume" : resume.getOriginalFilename()) + "\"")
                .body(resume.getFileData());
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeJobMatch(@RequestBody Map<String, String> request,
                                                  Authentication authentication) {
        String jobDescription = request.get("jobDescription");
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new IllegalArgumentException("Job description cannot be empty");
        }
        User user = getUser(authentication.getName());
        MasterResume masterResume = masterResumeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Please upload a master resume first."));
        return ResponseEntity.ok(llmService.analyzeAndTailor(masterResume.getExtractedText(), jobDescription));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) throw new IllegalArgumentException("Invalid file name");
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        if (!extension.equals(".pdf") && !extension.equals(".docx")) {
            throw new IllegalArgumentException("Only PDF and DOCX files are allowed");
        }
        return extension;
    }
}
