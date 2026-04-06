package io.github.qifan777.knowledge.ai.aiMessage;

import java.util.Map;
import java.util.HashMap;

public final class PoetryTeachingProfiles {

    private PoetryTeachingProfiles() {
    }

    public static GradeProfile gradeProfile(String gradeLevel) {
        return switch (gradeLevel) {
            case "小学低年级" -> new GradeProfile(
                    "使用短句和生活化比喻，不解释抽象文学术语",
                    "优先讲清字面意思、画面感和情绪词",
                    "每次回答控制在3到5句，必要时分点列出",
                    "例如把‘意象’改说成‘诗里看到的景物’"
            );
            case "小学中年级" -> new GradeProfile(
                    "可以加入基础修辞和情感词，但避免过多专业术语",
                    "优先讲清关键词、诗句顺序和作者想表达的心情",
                    "每次回答控制在4到6句，允许出现简单分层讲解",
                    "例如把‘借景抒情’解释成‘借看到的景物表达心情’"
            );
            case "初中" -> new GradeProfile(
                    "允许使用主旨、意象、修辞、表现手法等术语，并给简短定义",
                    "优先讲清结构、情感变化、表达技巧和考试答题点",
                    "每次回答控制在5到8句，可附简短答题模板",
                    "例如同时解释‘意象’和‘情景交融’"
            );
            default -> new GradeProfile(
                    "语言保持清楚自然，可以使用少量文学术语并同步解释",
                    "优先讲清关键词、意境、情感和常见考点",
                    "每次回答控制在4到6句，适合小学高年级阅读",
                    "例如解释‘以景写情’时同时给生活化例子"
            );
        };
    }

    public static ModeProfile modeProfile(String mode) {
        return switch (mode) {
            case "question" -> new ModeProfile(
                    "不要直接给完整答案，先问学生1到2个启发式小问题",
                    "最后给一个不超过80字的小结"
            );
            case "recite" -> new ModeProfile(
                    "按首字提示、关键词提示、整句提示三层递进",
                    "每次只前进一步，除非用户明确要求完整答案"
            );
            case "practice" -> new ModeProfile(
                    "默认生成1题，题型优先选择题或填空题",
                    "输出顺序固定为：题目、答案、解析、举一反三"
            );
            default -> new ModeProfile(
                    "先讲字词，再讲画面，再讲情感和主旨",
                    "最后补一个适合该学段的记忆点"
            );
        };
    }

    public static Map<String, Object> buildPromptVariables(String gradeLevel, String mode) {
        GradeProfile gradeProfile = gradeProfile(gradeLevel);
        ModeProfile modeProfile = modeProfile(mode);
        Map<String, Object> variables = new HashMap<>();
        variables.put("gradeLevel", gradeLevel);
        variables.put("languageRule", gradeProfile.languageRule());
        variables.put("focusRule", gradeProfile.focusRule());
        variables.put("lengthRule", gradeProfile.lengthRule());
        variables.put("exampleRule", gradeProfile.exampleRule());
        variables.put("modeRule", modeProfile.modeRule());
        variables.put("outputRule", modeProfile.outputRule());
        return variables;
    }

    public record GradeProfile(
            String languageRule,
            String focusRule,
            String lengthRule,
            String exampleRule
    ) {
    }

    public record ModeProfile(
            String modeRule,
            String outputRule
    ) {
    }
}
