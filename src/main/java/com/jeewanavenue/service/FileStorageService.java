package com.jeewanavenue.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // The annotation value now correctly matches the properties file
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            // Create subdirectories for different document types
            Files.createDirectories(this.fileStorageLocation.resolve("docs/constitution"));
            Files.createDirectories(this.fileStorageLocation.resolve("docs/notification"));
            Files.createDirectories(this.fileStorageLocation.resolve("docs/amendment"));
            Files.createDirectories(this.fileStorageLocation.resolve("leases"));
            Files.createDirectories(this.fileStorageLocation.resolve("profiles"));
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String subfolder) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(subfolder);
            Files.createDirectories(targetLocation);

            Path filePath = targetLocation.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return Paths.get(subfolder, fileName).toString().replace("\\", "/");

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            System.out.println("Looking for file at: " + filePath.toString());
            System.out.println("File exists: " + Files.exists(filePath));
            
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                System.out.println("Resource found and accessible");
                return resource;
            } else {
                System.err.println("File not found at: " + filePath.toString());
                throw new RuntimeException("File not found " + fileName + " at path: " + filePath.toString());
            }
        } catch (MalformedURLException ex) {
            System.err.println("MalformedURLException for file: " + fileName);
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }
}