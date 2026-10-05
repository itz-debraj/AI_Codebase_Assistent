package com.example.ai_coding_assistent.repository;

import com.example.ai_coding_assistent.entity.CodeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeChunkRepository extends JpaRepository<CodeChunk, UUID> {
    List<CodeChunk> findByRepositoryId(UUID repositoryId);
    List<CodeChunk> findByFileId(UUID fileId);
    int countByRepositoryId(UUID repositoryId);
    void deleteByRepositoryId(UUID repositoryId);
}
