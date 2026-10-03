package com.jobmatcher.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

@Service
public class ResumeParserService {

    private final Tika tika = new Tika();

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private static final int MAX_TEXT_LENGTH = 100_000;

   private static final Set<String> ALLOWED_TYPES = Set.of(
        "application/pdf",
        "application/x-tika-ooxml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
);

    public String extractText(MultipartFile file) {

        // 1. Check empty file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Uploaded file cannot be empty"
            );
        }

        // 2. Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size cannot exceed 5 MB"
            );
        }

        try {

            byte[] fileBytes = file.getBytes();

            // 3. Detect actual file type
            String detectedType = tika.detect(
                    new ByteArrayInputStream(fileBytes)
            );
            System.out.println("Detected file type: " + detectedType);

            // 4. Validate file type
            if (!ALLOWED_TYPES.contains(detectedType)) {
                throw new IllegalArgumentException(
                        "Only PDF and DOCX files are allowed"
                );
            }

            // 5. Extract text
            String extractedText = tika.parseToString(
                    new ByteArrayInputStream(fileBytes)
            );

            if (extractedText == null || extractedText.isBlank()) {
                throw new IllegalArgumentException(
                        "Could not extract readable text from the file"
                );
            }

            // 6. Limit extracted text
            if (extractedText.length() > MAX_TEXT_LENGTH) {
                extractedText = extractedText.substring(
                        0,
                        MAX_TEXT_LENGTH
                );
            }

            return extractedText.trim();

        } catch (IllegalArgumentException exception) {

            throw exception;

        } catch (IOException exception) {

            throw new RuntimeException(
                    "Failed to read resume file",
                    exception
            );

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Failed to extract text from resume file",
                    exception
            );
        }
    }
}