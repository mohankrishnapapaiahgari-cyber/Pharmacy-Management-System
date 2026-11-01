package com.example.demo.repository;

import com.example.demo.model.SalesReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {

    List<SalesReturn> findAllByOrderByReturnDateDesc();
}
