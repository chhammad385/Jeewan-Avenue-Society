package com.jeewanavenue.controller;

import com.jeewanavenue.entity.Renter;
import com.jeewanavenue.service.FileStorageService;
import com.jeewanavenue.service.RenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/renters")
public class RenterController {

    @Autowired
    private RenterService renterService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public List<Renter> getAllRenters() {
        return renterService.findAll();
    }

    @PostMapping
    public ResponseEntity<Renter> createRenter(
            @RequestPart("renter") Renter renter,
            @RequestPart("leaseDoc") MultipartFile leaseDoc) {

        String filePath = fileStorageService.storeFile(leaseDoc, "leases");
        renter.setDocumentPath(filePath);

        Renter savedRenter = renterService.save(renter);
        return new ResponseEntity<>(savedRenter, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRenter(@PathVariable Long id) {
        renterService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}