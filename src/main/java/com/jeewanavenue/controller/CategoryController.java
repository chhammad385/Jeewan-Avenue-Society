package com.jeewanavenue.controller;

import com.jeewanavenue.entity.Category;
import com.jeewanavenue.entity.Staff;
import com.jeewanavenue.service.CategoryService;
import com.jeewanavenue.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StaffRepository staffRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/staff")
    public ResponseEntity<List<Staff>> getStaffByCategory(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(category -> {
                    List<Staff> staff = staffRepository.findByCategory(category.getName());
                    return ResponseEntity.ok(staff);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}/staff")
    public ResponseEntity<List<Staff>> getStaffByCategoryName(@PathVariable String name) {
        List<Staff> staff = staffRepository.findByCategory(name);
        return ResponseEntity.ok(staff);
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try {
            Category createdCategory = categoryService.save(category);
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        try {
            Category updatedCategory = categoryService.update(id, categoryDetails);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.delete(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Category deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/check/{name}")
    public ResponseEntity<Map<String, Boolean>> checkCategoryExists(@PathVariable String name) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", categoryService.existsByName(name));
        return ResponseEntity.ok(response);
    }
}
