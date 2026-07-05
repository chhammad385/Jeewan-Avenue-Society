package com.jeewanavenue.service;

import com.jeewanavenue.entity.Category;
import com.jeewanavenue.repository.CategoryRepository;
import com.jeewanavenue.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StaffRepository staffRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Transactional
    public Category save(Category category) {
        // Check if category with same name already exists
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if new name conflicts with existing category
        if (!category.getName().equals(categoryDetails.getName()) && 
            categoryRepository.existsByName(categoryDetails.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDetails.getName() + "' already exists");
        }

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if any staff members are using this category
        long staffCount = staffRepository.countByCategory(category.getName());
        if (staffCount > 0) {
            throw new IllegalArgumentException("Cannot delete category '" + category.getName() + 
                "' because " + staffCount + " staff member(s) are assigned to it");
        }

        categoryRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
