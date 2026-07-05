package com.jeewanavenue.repository;

import com.jeewanavenue.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByCategory(String category);
    long countByCategory(String category);
}