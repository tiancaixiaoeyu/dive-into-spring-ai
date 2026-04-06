package io.github.qifan777.knowledge.ai.exercise.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExerciseRecordSaveRequest {
    private String sessionId;
    private String question;
    private String userAnswer;
    private String correctAnswer;
    private Boolean correct;
    private String exerciseType;
    private String explanation;
    private String extension;
    private String gradeLevel;
    private String mode;
    private List<String> options;
}
