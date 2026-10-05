package com.example.ai_coding_assistent.repository;

import com.example.ai_coding_assistent.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);
}
