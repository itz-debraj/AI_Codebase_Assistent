package com.example.ai_coding_assistent.service;

import com.example.ai_coding_assistent.config.AppProperties;
import com.example.ai_coding_assistent.exception.InvalidRepositoryException;
import com.example.ai_coding_assistent.indexing.CodeIndexingService;
import com.example.ai_coding_assistent.repository.CodeRepositoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private CodeRepositoryRepository repositoryRepository;

    @Mock
    private CodeIndexingService codeIndexingService;

    private RepositoryService repositoryService;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        repositoryService = new RepositoryService(repositoryRepository, appProperties, codeIndexingService);
    }

    @Test
    void shouldRejectNonZipFiles() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello world".getBytes()
        );

        assertThrows(InvalidRepositoryException.class, () ->
                repositoryService.uploadRepository(file, "test-repo"));
    }

    @Test
    void shouldRejectEmptyFiles() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.zip",
                "application/zip",
                new byte[0]
        );

        assertThrows(InvalidRepositoryException.class, () ->
                repositoryService.uploadRepository(file, "test-repo"));
    }

    @Test
    void shouldRejectBlankRepositoryName() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "repo.zip",
                "application/zip",
                new byte[]{1, 2, 3}
        );

        assertThrows(InvalidRepositoryException.class, () ->
                repositoryService.uploadRepository(file, "   "));
    }
}
