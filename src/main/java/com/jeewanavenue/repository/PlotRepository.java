package com.jeewanavenue.repository;

import com.jeewanavenue.entity.Plot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlotRepository extends JpaRepository<Plot, String> {
}