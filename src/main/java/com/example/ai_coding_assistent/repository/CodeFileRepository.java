package com.example.ai_coding_assistent.repository;

import com.example.ai_coding_assistent.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeFileRepository extends JpaRepository<CodeFile, UUID> {
    List<CodeFile> findByRepositoryId(UUID repositoryId);
    int countByRepositoryId(UUID repositoryId);
}
