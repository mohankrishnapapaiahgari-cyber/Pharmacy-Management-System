package com.example.demo.repository;

import com.example.demo.model.ShelfLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShelfLocationRepository extends JpaRepository<ShelfLocation, Long> {
}
