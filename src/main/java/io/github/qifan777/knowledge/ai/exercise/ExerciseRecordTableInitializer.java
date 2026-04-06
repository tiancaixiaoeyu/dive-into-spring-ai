package io.github.qifan777.knowledge.ai.exercise;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ExerciseRecordTableInitializer {
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS exercise_record (
                    id VARCHAR(64) PRIMARY KEY,
                    question VARCHAR(1000) NOT NULL,
                    user_answer VARCHAR(255) NOT NULL,
                    correct_answer VARCHAR(255) NOT NULL,
                    correct BIT NOT NULL,
                    exercise_type VARCHAR(64) NOT NULL,
                    explanation TEXT NULL,
                    extension TEXT NULL,
                    grade_level VARCHAR(64) NULL,
                    mode VARCHAR(64) NULL,
                    options JSON NULL,
                    ai_session_id VARCHAR(64) NOT NULL,
                    creator_id VARCHAR(64) NOT NULL,
                    editor_id VARCHAR(64) NOT NULL,
                    created_time DATETIME NOT NULL,
                    edited_time DATETIME NOT NULL,
                    INDEX idx_exercise_record_creator (creator_id),
                    INDEX idx_exercise_record_wrong (creator_id, correct),
                    INDEX idx_exercise_record_session (ai_session_id)
                )
                """);
        log.info("exercise_record 表已就绪");
    }
}
