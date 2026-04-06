package io.github.qifan777.knowledge.ai.exercise;

import io.github.qifan777.knowledge.ai.session.AiSession;
import io.github.qifan777.knowledge.infrastructure.jimmer.BaseEntity;
import jakarta.validation.constraints.Null;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.IdView;
import org.babyfish.jimmer.sql.JoinColumn;
import org.babyfish.jimmer.sql.ManyToOne;
import org.babyfish.jimmer.sql.OnDissociate;
import org.babyfish.jimmer.sql.DissociateAction;
import org.babyfish.jimmer.sql.Serialized;

import java.util.List;

@Entity
public interface ExerciseRecord extends BaseEntity {

    String question();

    String userAnswer();

    String correctAnswer();

    boolean correct();

    String exerciseType();

    @Null
    String explanation();

    @Null
    String extension();

    @Null
    String gradeLevel();

    @Null
    String mode();

    @Serialized
    @Null
    List<String> options();

    @IdView
    String sessionId();

    @ManyToOne
    @JoinColumn(name = "ai_session_id")
    @OnDissociate(DissociateAction.DELETE)
    AiSession session();
}
