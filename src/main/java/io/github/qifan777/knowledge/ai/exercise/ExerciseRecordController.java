package io.github.qifan777.knowledge.ai.exercise;

import io.github.qifan777.knowledge.ai.exercise.dto.ExerciseRecordSaveRequest;
import io.qifan.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.babyfish.jimmer.client.FetchBy;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("exercise-record")
@RestController
@AllArgsConstructor
public class ExerciseRecordController {
    private final ExerciseRecordRepository exerciseRecordRepository;

    @PostMapping
    public String save(@RequestBody ExerciseRecordSaveRequest request) {
        validate(request);
        ExerciseRecord exerciseRecord = ExerciseRecordDraft.$.produce(draft -> {
            draft.setQuestion(request.getQuestion());
            draft.setUserAnswer(request.getUserAnswer());
            draft.setCorrectAnswer(request.getCorrectAnswer());
            draft.setCorrect(Boolean.TRUE.equals(request.getCorrect()));
            draft.setExerciseType(request.getExerciseType());
            draft.setExplanation(request.getExplanation());
            draft.setExtension(request.getExtension());
            draft.setGradeLevel(request.getGradeLevel());
            draft.setMode(request.getMode());
            draft.setOptions(request.getOptions());
            draft.applySession(session -> session.setId(request.getSessionId()));
        });
        return exerciseRecordRepository.save(exerciseRecord).id();
    }

    @GetMapping("user")
    public List<@FetchBy(value = "FETCHER", ownerType = ExerciseRecordRepository.class) ExerciseRecord> findByUser(
            @RequestParam(defaultValue = "false") boolean wrongOnly,
            @RequestParam(defaultValue = "10") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return exerciseRecordRepository.findByCurrentUser(wrongOnly, safeLimit);
    }

    private void validate(ExerciseRecordSaveRequest request) {
        if (!StringUtils.hasText(request.getSessionId())) {
            throw new BusinessException("会话ID不能为空");
        }
        if (!StringUtils.hasText(request.getQuestion())) {
            throw new BusinessException("题目不能为空");
        }
        if (!StringUtils.hasText(request.getUserAnswer())) {
            throw new BusinessException("作答内容不能为空");
        }
        if (!StringUtils.hasText(request.getCorrectAnswer())) {
            throw new BusinessException("标准答案不能为空");
        }
        if (!StringUtils.hasText(request.getExerciseType())) {
            throw new BusinessException("题型不能为空");
        }
    }
}
