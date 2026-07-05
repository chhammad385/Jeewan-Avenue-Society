package com.jeewanavenue.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class FileTestController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/files")
    public List<String> listAllFiles() {
        List<String> files = new ArrayList<>();
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            files.add("Upload directory: " + uploadPath.toString());
            
            // List all files recursively
            Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    Path relativePath = uploadPath.relativize(file);
                    files.add("File: " + relativePath.toString().replace("\\", "/"));
                });
        } catch (Exception e) {
            files.add("Error: " + e.getMessage());
        }
        return files;
    }

    @GetMapping("/check-file")
    public String checkSpecificFile() {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path testFile = uploadPath.resolve("docs/constitution/C.P LIST (28-Aug-2025) (1).pdf");
            
            return "File exists: " + Files.exists(testFile) + 
                   "\nFull path: " + testFile.toString() +
                   "\nIs readable: " + Files.isReadable(testFile);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}