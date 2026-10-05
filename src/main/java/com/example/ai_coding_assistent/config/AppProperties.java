package com.example.ai_coding_assistent.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Type-safe configuration properties for the application.
 * Maps to the "app" prefix in application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private Storage storage = new Storage();
    private Indexing indexing = new Indexing();
    private Chunking chunking = new Chunking();
    private Rag rag = new Rag();

    @Getter
    @Setter
    public static class Storage {
        private String uploadDir = "./uploads";
        private String tempDir = "./temp";
        private int maxFileSizeMb = 50;
    }

    @Getter
    @Setter
    public static class Indexing {
        private List<String> supportedExtensions = List.of(
                ".java", ".js", ".jsx", ".ts", ".tsx",
                ".py", ".go", ".kt",
                ".xml", ".yml", ".yaml", ".json", ".properties",
                ".md", ".sql"
        );
        private List<String> ignoredDirectories = List.of(
                "target", "build", "node_modules", ".git",
                "dist", "out", "coverage", ".idea", ".vscode",
                "__pycache__", ".gradle"
        );
        private List<String> ignoredFiles = List.of(
                "package-lock.json", "yarn.lock", "pnpm-lock.yaml", ".DS_Store"
        );
        private int maxFileSizeKb = 500;
    }

    @Getter
    @Setter
    public static class Chunking {
        private int maxChunkSize = 2000;
        private int overlapSize = 200;
    }

    @Getter
    @Setter
    public static class Rag {
        private int topK = 8;
        private double similarityThreshold = 0.5;
    }
}
