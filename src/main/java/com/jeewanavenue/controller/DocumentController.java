package com.jeewanavenue.controller;

import com.jeewanavenue.entity.Document;
import com.jeewanavenue.service.DocumentService;
import com.jeewanavenue.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public List<Document> getAllDocuments() {
        return documentService.findAll();
    }

    @PostMapping
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file
    ) {
        // Determine subdirectory based on category
        String subdirectory = "docs/" + category.toLowerCase();
        String filePath = fileStorageService.storeFile(file, subdirectory);

        Document doc = new Document();
        doc.setTitle(title);
        doc.setCategory(category);
        doc.setDescription(description);
        doc.setFilePath(filePath);
        doc.setUploadDate(LocalDateTime.now());

        Document savedDocument = documentService.save(doc);
        return new ResponseEntity<>(savedDocument, HttpStatus.CREATED);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        try {
            // Load file as Resource
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                // Fallback to default content type if type could not be determined
                contentType = "application/octet-stream";
            }

            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            // Return 404 if file not found
            return ResponseEntity.notFound().build();
        }
    }

    // New endpoint for direct file access (like renter documents)
    @GetMapping("/file/**")
    public ResponseEntity<Resource> downloadFileByPath(HttpServletRequest request) {
        try {
            // Extract the file path from the request URI
            String requestUrl = request.getRequestURI();
            String filePath = requestUrl.substring("/api/documents/file/".length());
            
            System.out.println("=== FILE DOWNLOAD DEBUG ===");
            System.out.println("Request URL: " + requestUrl);
            System.out.println("Extracted file path: " + filePath);
            System.out.println("Attempting to download file: " + filePath);
            
            // Load file as Resource using the full path
            Resource resource = fileStorageService.loadFileAsResource(filePath);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                contentType = "application/octet-stream";
            }

            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            System.out.println("Content type: " + contentType);
            System.out.println("File found and ready to serve");
            System.out.println("===========================");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            System.err.println("=== FILE DOWNLOAD ERROR ===");
            System.err.println("Error downloading file: " + ex.getMessage());
            ex.printStackTrace();
            System.err.println("============================");
            return ResponseEntity.notFound().build();
        }
    }

    // Alternative endpoint with explicit path parameter
    @GetMapping("/download-file")
    public ResponseEntity<Resource> downloadFileByParam(@RequestParam("path") String filePath, HttpServletRequest request) {
        try {
            System.out.println("=== ALTERNATIVE DOWNLOAD DEBUG ===");
            System.out.println("File path parameter: " + filePath);
            
            // Load file as Resource using the full path
            Resource resource = fileStorageService.loadFileAsResource(filePath);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                contentType = "application/octet-stream";
            }

            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            System.out.println("Content type: " + contentType);
            System.out.println("File found and ready to serve");
            System.out.println("==================================");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            System.err.println("=== ALTERNATIVE DOWNLOAD ERROR ===");
            System.err.println("Error downloading file: " + ex.getMessage());
            ex.printStackTrace();
            System.err.println("===================================");
            return ResponseEntity.notFound().build();
        }
    }

    // Test endpoint to directly serve the file we know exists
    @GetMapping("/test-file")
    public ResponseEntity<Resource> testFileDownload(HttpServletRequest request) {
        try {
            // We know this file exists: docs/constitution/C.P LIST (28-Aug-2025) (1).pdf
            String filePath = "docs/constitution/C.P LIST (28-Aug-2025) (1).pdf";
            
            System.out.println("=== TEST FILE DOWNLOAD ===");
            System.out.println("Testing file path: " + filePath);
            
            // Load file as Resource using the full path
            Resource resource = fileStorageService.loadFileAsResource(filePath);

            // Try to determine file's content type
            String contentType = "application/pdf";

            System.out.println("Content type: " + contentType);
            System.out.println("File found and ready to serve");
            System.out.println("==========================");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            System.err.println("=== TEST FILE DOWNLOAD ERROR ===");
            System.err.println("Error downloading test file: " + ex.getMessage());
            ex.printStackTrace();
            System.err.println("================================");
            return ResponseEntity.notFound().build();
        }
    }

    // New preview endpoint for viewing files inline in browser
    @GetMapping("/preview/**")
    public ResponseEntity<Resource> previewFileByPath(HttpServletRequest request) {
        try {
            // Extract the file path from the request URI
            String requestUrl = request.getRequestURI();
            String encodedFilePath = requestUrl.substring("/api/documents/preview/".length());
            // Decode the URL-encoded file path
            String filePath = java.net.URLDecoder.decode(encodedFilePath, "UTF-8");
            
            System.out.println("=== FILE PREVIEW DEBUG ===");
            System.out.println("Request URL: " + requestUrl);
            System.out.println("Encoded file path: " + encodedFilePath);
            System.out.println("Decoded file path: " + filePath);
            System.out.println("Attempting to preview file: " + filePath);
            
            // Load file as Resource using the full path
            Resource resource = fileStorageService.loadFileAsResource(filePath);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                contentType = "application/octet-stream";
            }

            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            System.out.println("Content type: " + contentType);
            System.out.println("File found and ready to serve for preview");
            System.out.println("===========================");

            // Use 'inline' instead of 'attachment' to display in browser
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            System.err.println("=== FILE PREVIEW ERROR ===");
            System.err.println("Error previewing file: " + ex.getMessage());
            ex.printStackTrace();
            System.err.println("===========================");
            return ResponseEntity.notFound().build();
        }
    }

    // Alternative preview endpoint with explicit path parameter
    @GetMapping("/preview-file")
    public ResponseEntity<Resource> previewFileByParam(@RequestParam("path") String encodedFilePath, HttpServletRequest request) {
        try {
            // Decode the URL-encoded file path
            String filePath = java.net.URLDecoder.decode(encodedFilePath, "UTF-8");
            
            System.out.println("=== ALTERNATIVE PREVIEW DEBUG ===");
            System.out.println("Encoded file path parameter: " + encodedFilePath);
            System.out.println("Decoded file path: " + filePath);
            
            // Load file as Resource using the full path
            Resource resource = fileStorageService.loadFileAsResource(filePath);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                contentType = "application/octet-stream";
            }

            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            System.out.println("Content type: " + contentType);
            System.out.println("File found and ready to serve for preview");
            System.out.println("====================================");

            // Use 'inline' instead of 'attachment' to display in browser
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            System.err.println("=== ALTERNATIVE PREVIEW ERROR ===");
            System.err.println("Error previewing file: " + ex.getMessage());
            ex.printStackTrace();
            System.err.println("=====================================");
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        // You might also want to delete the physical file from storage here
        documentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}