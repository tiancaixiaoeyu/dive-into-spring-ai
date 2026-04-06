package io.github.qifan777.knowledge.ai.exercise;

import cn.dev33.satoken.stp.StpUtil;
import org.babyfish.jimmer.spring.repository.JRepository;

import java.util.List;

public interface ExerciseRecordRepository extends JRepository<ExerciseRecord, String> {
    ExerciseRecordTable t = ExerciseRecordTable.$;
    ExerciseRecordFetcher FETCHER = ExerciseRecordFetcher.$.allScalarFields().sessionId();

    default List<ExerciseRecord> findByCurrentUser(boolean wrongOnly, int limit) {
        return sql().createQuery(t)
                .where(t.creatorId().eq(StpUtil.getLoginIdAsString()))
                .whereIf(wrongOnly, t.correct().eq(false))
                .orderBy(t.createdTime().desc())
                .select(t.fetch(FETCHER))
                .limit(limit)
                .execute();
    }
}
