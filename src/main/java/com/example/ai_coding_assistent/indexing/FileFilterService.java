package com.example.ai_coding_assistent.indexing;

import com.example.ai_coding_assistent.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Service to validate and filter source files during repository indexing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileFilterService {

    private final AppProperties appProperties;

    private static final Map<String, String> EXTENSION_LANGUAGE_MAP = Map.ofEntries(
            Map.entry(".java", "java"),
            Map.entry(".js", "javascript"),
            Map.entry(".jsx", "javascript"),
            Map.entry(".ts", "typescript"),
            Map.entry(".tsx", "typescript"),
            Map.entry(".py", "python"),
            Map.entry(".go", "go"),
            Map.entry(".kt", "kotlin"),
            Map.entry(".xml", "xml"),
            Map.entry(".yml", "yaml"),
            Map.entry(".yaml", "yaml"),
            Map.entry(".json", "json"),
            Map.entry(".properties", "properties"),
            Map.entry(".md", "markdown"),
            Map.entry(".sql", "sql")
    );

    /**
     * Determines whether a given relative path or file should be indexed.
     */
    public boolean shouldIndex(Path rootPath, Path filePath) {
        if (!Files.isRegularFile(filePath)) {
            return false;
        }

        // Relative path normalized with forward slashes
        Path relativePath = rootPath.relativize(filePath);
        String relativeStr = relativePath.toString().replace('\\', '/');
        String fileName = filePath.getFileName().toString();

        // 1. Check ignored directories anywhere in the path
        for (String ignoredDir : appProperties.getIndexing().getIgnoredDirectories()) {
            if (relativeStr.equals(ignoredDir) ||
                relativeStr.startsWith(ignoredDir + "/") ||
                relativeStr.contains("/" + ignoredDir + "/")) {
                return false;
            }
        }

        // 2. Check ignored file names
        for (String ignoredFile : appProperties.getIndexing().getIgnoredFiles()) {
            if (fileName.equalsIgnoreCase(ignoredFile)) {
                return false;
            }
        }

        // 3. Ignore hidden files (starting with dot) except specific config files
        if (fileName.startsWith(".") && !fileName.equals(".env.example")) {
            return false;
        }

        // 4. Check extension
        String extension = getFileExtension(fileName);
        if (!appProperties.getIndexing().getSupportedExtensions().contains(extension.toLowerCase(Locale.ROOT))) {
            return false;
        }

        // 5. Check file size
        try {
            long sizeBytes = Files.size(filePath);
            long maxBytes = (long) appProperties.getIndexing().getMaxFileSizeKb() * 1024;
            if (sizeBytes > maxBytes || sizeBytes == 0) {
                return false;
            }
        } catch (IOException e) {
            log.warn("Failed to check file size for {}: {}", filePath, e.getMessage());
            return false;
        }

        return true;
    }

    public String detectLanguage(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase(Locale.ROOT);
        return EXTENSION_LANGUAGE_MAP.getOrDefault(ext, "text");
    }

    public String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(dotIndex) : "";
    }
}
