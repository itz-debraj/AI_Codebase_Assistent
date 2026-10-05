package com.example.ai_coding_assistent.indexing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaCodeChunkerTest {

    private final JavaCodeChunker chunker = new JavaCodeChunker();

    @Test
    void shouldChunkJavaClassPreservingClassName() {
        String javaCode = """
                package com.example.service;
                
                import java.util.List;
                
                public class PaymentService {
                
                    public void processPayment(String orderId) {
                        System.out.println("Processing " + orderId);
                    }
                    
                    public void refundPayment(String orderId) {
                        System.out.println("Refunding " + orderId);
                    }
                }
                """;

        List<ChunkResult> chunks = chunker.chunk(javaCode, 1000, 100);

        assertFalse(chunks.isEmpty());
        ChunkResult first = chunks.get(0);
        assertEquals("java", first.getLanguage());
        assertEquals("PaymentService", first.getClassName());
        assertTrue(first.getStartLine() >= 1);
        assertTrue(first.getEndLine() >= first.getStartLine());
    }

    @Test
    void shouldHandleEmptyContentGracefully() {
        List<ChunkResult> chunks = chunker.chunk("", 1000, 100);
        assertTrue(chunks.isEmpty());
    }
}
