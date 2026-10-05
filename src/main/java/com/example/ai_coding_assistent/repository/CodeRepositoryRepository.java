package com.example.ai_coding_assistent.repository;

import com.example.ai_coding_assistent.entity.CodeRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CodeRepositoryRepository extends JpaRepository<CodeRepository, UUID> {
}
