package io.github.qifan777.knowledge.ai.aiMessage.dto;

import lombok.Data;

@Data
public class AiMessageParams {
    Boolean enableVectorStore;
    Boolean enableAgent;
    Boolean enableCitation;
    String mode;
    String gradeLevel;
}
