package com.example.kaumedicare.repository;


import com.example.kaumedicare.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
}