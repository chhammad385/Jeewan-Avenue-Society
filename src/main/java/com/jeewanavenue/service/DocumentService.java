package com.jeewanavenue.service;

import com.jeewanavenue.entity.Document;
import com.jeewanavenue.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public Document save(Document document) {
        return documentRepository.save(document);
    }

    public void deleteById(Long id) {
        documentRepository.deleteById(id);
    }
}