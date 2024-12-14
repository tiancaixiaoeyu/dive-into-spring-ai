package io.github.qifan777.knowledge.ai.aiMessage.dto;

import io.github.qifan777.knowledge.ai.aiMessage.dto.AiMessageParams;
import lombok.Data;

@Data
public class AiMessageWrapper {
    AiMessageInput message;
    AiMessageParams params;
}
