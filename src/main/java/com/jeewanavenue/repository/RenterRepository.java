package com.jeewanavenue.repository;

import com.jeewanavenue.entity.Renter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RenterRepository extends JpaRepository<Renter, Long> {
}