package io.github.qifan777.knowledge.ai.aiMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.qifan777.knowledge.ai.aiMessage.dto.AiMessageInput;
import io.github.qifan777.knowledge.ai.aiMessage.dto.AiMessageParams;
import io.github.qifan777.knowledge.ai.aiMessage.dto.AiMessageWrapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.Media;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.babyfish.jimmer.client.EnableImplicitApi;

@EnableImplicitApi

@RequestMapping("message")
@RestController
@AllArgsConstructor
@Slf4j
public class AiMessageController{
  private final AiMessageRepository aiMessageRepository;
  private final AiMessageChatMemory chatMemory;
  private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

@DeleteMapping ("history/{sessionId}")
 public void deleteHistory(@PathVariable String sessionId){
    aiMessageRepository.deleteBySessionId(sessionId);
}

    @PostMapping
    public void  save(@RequestBody AiMessageInput input) {
        LocalDateTime now = LocalDateTime.now();
        AiMessage message = AiMessageDraft.$.produce(draft -> {
            draft.setCreatedTime(now);
            draft.setEditedTime(now);
            draft.setType(input.getType());
            draft.setTextContent(input.getTextContent());
            draft.setMedias(input.getMedias());
            draft.applySession(session -> session.setId(input.getSessionId()));
        });
        aiMessageRepository.save(message);
    }

    /**
     * 为了支持文件问答，需要同时接收json（AiMessageWrapper json体）和 MultipartFile（文件）
     * Content-Type 从 application/json 修改为 multipart/form-data
     * 之前接收请求参数是用@RequestBody, 现在使用@RequestPart 接收json字符串再手动转成AiMessageWrapper.
     * SpringMVC的@RequestPart是支持自动将Json字符串转换为Java对象，也就是说可以等效`@RequestBody`，
     * 但是由于前端FormData无法设置Part的Content-Type，所以只能手动转json字符串再转成Java对象。
     *
     * @param input 消息包含文本信息，会话id，多媒体信息（图片语言）。参考src/main/dto/AiMessage.dto
     * @param file  文件问答
     * @return SSE流
     */
    @SneakyThrows
    @PostMapping(value = "chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestPart String input, @RequestPart(required = false) MultipartFile file) {
        AiMessageWrapper aiMessageWrapper = objectMapper.readValue(input, AiMessageWrapper.class);
        AiMessageParams params = normalizeParams(aiMessageWrapper.getParams());
        List<Map<String, String>> citations = searchCitations(aiMessageWrapper.getMessage().getTextContent(), params);

        Flux<ServerSentEvent<String>> citationFlux = citations.isEmpty()
                ? Flux.empty()
                : Flux.just(ServerSentEvent.builder(toJson(Map.of("items", citations)))
                .event("citation")
                .build());

        Map<String, Object> exerciseCard = buildExerciseCard(aiMessageWrapper.getMessage().getTextContent(), params, citations);
        Flux<ServerSentEvent<String>> exerciseFlux = exerciseCard.isEmpty()
                ? Flux.empty()
                : Flux.just(ServerSentEvent.builder(toJson(exerciseCard))
                .event("exercise")
                .build());

        Flux<ServerSentEvent<String>> chatFlux = ChatClient.create(chatModel).prompt()
                .system(promptSystemSpec -> {
                    useTeachingStrategy(promptSystemSpec, params);
                    useCitationContext(promptSystemSpec, citations);
                    useFile(promptSystemSpec, file);
                })
                .user(promptUserSpec -> toPrompt(promptUserSpec, aiMessageWrapper.getMessage()))
                .advisors(advisorSpec -> {
                    useChatHistory(advisorSpec, aiMessageWrapper.getMessage().getSessionId());
                    useVectorStore(advisorSpec, params.getEnableVectorStore());
                })
                .stream()
                .chatResponse()
                .map(chatResponse -> ServerSentEvent.builder(toJson(chatResponse))
                        .event("message")
                        .build());

        return Flux.concat(citationFlux, exerciseFlux, chatFlux);
    }

    @SneakyThrows
    public String toJson(Object response) {
        return objectMapper.writeValueAsString(response);
    }

    public void toPrompt(ChatClient.PromptUserSpec promptUserSpec, AiMessageInput input) {
        // AiMessageInput转成Message
        Message message = AiMessageChatMemory.toSpringAiMessage(input.toEntity());
        if (message instanceof UserMessage userMessage &&
                !CollectionUtils.isEmpty(userMessage.getMedia())) {
            // 用户发送的图片/语言
            Media[] medias = new Media[userMessage.getMedia().size()];
            promptUserSpec.media(userMessage.getMedia().toArray(medias));
        }
        // 用户发送的文本
        promptUserSpec.text(message.getContent());
    }

    public void useChatHistory(ChatClient.AdvisorSpec advisorSpec, String sessionId) {
        // 1. 如果需要存储会话和消息到数据库，自己可以实现ChatMemory接口，这里使用自己实现的AiMessageChatMemory，数据库存储。
        // 2. 传入会话id，MessageChatMemoryAdvisor会根据会话id去查找消息。
        // 3. 只需要携带最近10条消息
        // MessageChatMemoryAdvisor会在消息发送给大模型之前，从ChatMemory中获取会话的历史消息，然后一起发送给大模型。
        advisorSpec.advisors(new MessageChatMemoryAdvisor(chatMemory, sessionId, 10));
    }

    public void useVectorStore(ChatClient.AdvisorSpec advisorSpec, Boolean enableVectorStore) {
        if (!Boolean.TRUE.equals(enableVectorStore)) return;
        String promptWithContext = """
                下面是上下文信息
                ---------------------
                {question_answer_context}
                ---------------------
                给定上下文和历史信息回答问题。若上下文中不存在答案，请明确说明“未在知识库检索到依据”。
                """;
        advisorSpec.advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults(), promptWithContext));
    }

    public void useTeachingStrategy(ChatClient.PromptSystemSpec spec, AiMessageParams params) {
        String mode = params.getMode();
        String gradeLevel = params.getGradeLevel();
        Map<String, Object> promptVariables = PoetryTeachingProfiles.buildPromptVariables(gradeLevel, mode);
        Message message = new PromptTemplate("""
                你是中小学诗词教学助手，请遵守：
                1. 学段：{gradeLevel}
                2. 语言规则：{languageRule}
                3. 讲解重点：{focusRule}
                4. 篇幅要求：{lengthRule}
                5. 教学模式策略：{modeRule}
                6. 输出结构：{outputRule}
                7. 解释方式示例：{exampleRule}
                8. 如果存在知识库依据，回答后附“依据”小节，列出引用句
                """)
                .createMessage(promptVariables);
        spec.text(message.getContent());
    }

    public void useCitationContext(ChatClient.PromptSystemSpec spec, List<Map<String, String>> citations) {
        if (CollectionUtils.isEmpty(citations)) return;
        List<String> citationContents = citations.stream()
                .map(item -> item.get("content"))
                .toList();
        Message message = new PromptTemplate("""
                以下是检索到的知识依据，可按需引用：
                ---------------------
                {citations}
                ---------------------
                """)
                .createMessage(Map.of("citations", String.join("\n\n", citationContents)));
        spec.text(message.getContent());
    }

    public AiMessageParams normalizeParams(AiMessageParams params) {
        AiMessageParams normalized = new AiMessageParams();
        if (params == null) {
            normalized.setEnableVectorStore(Boolean.FALSE);
            normalized.setEnableCitation(Boolean.FALSE);
            normalized.setEnableAgent(Boolean.FALSE);
            normalized.setMode("explain");
            normalized.setGradeLevel("小学高年级");
            return normalized;
        }
        normalized.setEnableVectorStore(Boolean.TRUE.equals(params.getEnableVectorStore()));
        normalized.setEnableCitation(Boolean.TRUE.equals(params.getEnableCitation()));
        normalized.setEnableAgent(Boolean.TRUE.equals(params.getEnableAgent()));
        normalized.setMode(params.getMode() == null ? "explain" : params.getMode());
        normalized.setGradeLevel(params.getGradeLevel() == null ? "小学高年级" : params.getGradeLevel());
        return normalized;
    }

    public List<Map<String, String>> searchCitations(String query, AiMessageParams params) {
        if (!Boolean.TRUE.equals(params.getEnableVectorStore()) || !Boolean.TRUE.equals(params.getEnableCitation())) {
            return Collections.emptyList();
        }
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<Document> documents = vectorStore.similaritySearch(SearchRequest.query(query).withTopK(3));
            if (CollectionUtils.isEmpty(documents)) {
                return Collections.emptyList();
            }
            List<Map<String, String>> results = new ArrayList<>();
            for (Document document : documents) {
                Map<String, String> item = new HashMap<>();
                item.put("title", resolveSource(document));
                item.put("content", clip(document.getContent(), 180));
                item.put("sourceUrl", resolveSourceUrl(document));
                item.put("chunkIndex", resolveChunkIndex(document));
                item.put("sourceType", resolveSourceType(document));
                results.add(item);
            }
            return results;
        } catch (Exception e) {
            log.warn("检索依据失败", e);
            return Collections.emptyList();
        }
    }

    public String resolveSource(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            return "知识库片段";
        }
        Object source = metadata.get("source");
        if (source == null) {
            source = metadata.get("file_name");
        }
        if (source == null) {
            source = metadata.get("name");
        }
        return source == null ? "知识库片段" : source.toString();
    }

    public String resolveSourceUrl(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }
        Object sourceUrl = metadata.get("sourceUrl");
        return sourceUrl == null ? "" : sourceUrl.toString();
    }

    public String resolveChunkIndex(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }
        Object chunkIndex = metadata.get("chunkIndex");
        return chunkIndex == null ? "" : chunkIndex.toString();
    }

    public String resolveSourceType(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            return "";
        }
        Object sourceType = metadata.get("sourceType");
        return sourceType == null ? "" : sourceType.toString();
    }

    public String clip(String content, int max) {
        if (content == null) {
            return "";
        }
        if (content.length() <= max) {
            return content;
        }
        return content.substring(0, max) + "...";
    }

    @SneakyThrows
    public Map<String, Object> buildExerciseCard(String query, AiMessageParams params, List<Map<String, String>> citations) {
        if (!"practice".equals(params.getMode()) || query == null || query.isBlank()) {
            return Collections.emptyMap();
        }
        String citationText = citations.isEmpty()
                ? "暂无额外依据"
                : citations.stream()
                .map(item -> item.get("title") + "：" + item.get("content"))
                .toList()
                .toString();
        Map<String, Object> variables = PoetryTeachingProfiles.buildPromptVariables(params.getGradeLevel(), params.getMode());
        variables.put("query", query);
        variables.put("citations", citationText);
        String content = ChatClient.create(chatModel).prompt()
                .system(promptSystemSpec -> promptSystemSpec.text(new PromptTemplate("""
                        你是中小学诗词练习出题助手。
                        学段：{gradeLevel}
                        语言规则：{languageRule}
                        讲解重点：{focusRule}
                        输出结构：{outputRule}
                        请严格输出 JSON，不要输出 markdown，不要输出多余解释。
                        JSON 结构如下：
                        {
                          "type":"single_choice 或 fill_blank",
                          "stem":"题干",
                          "options":["A. ...","B. ...","C. ...","D. ..."],
                          "answer":"标准答案",
                          "explanation":"解析",
                          "extension":"举一反三的小练习"
                        }
                        若题型为 fill_blank，options 输出空数组。
                        """).createMessage(variables).getContent()))
                .user(promptUserSpec -> promptUserSpec.text("""
                        请根据下面的学习目标生成一题：
                        %s

                        可参考依据：
                        %s
                        """.formatted(query, citationText)))
                .call()
                .content();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> exercise = objectMapper.readValue(content, LinkedHashMap.class);
            return exercise;
        } catch (Exception e) {
            log.warn("练习题结构化解析失败，使用兜底题卡", e);
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("type", "fill_blank");
            fallback.put("stem", "请根据当前学习内容，试着默写或回答：%s".formatted(query));
            fallback.put("options", List.of());
            fallback.put("answer", "请结合上方AI讲解作答");
            fallback.put("explanation", "系统暂未成功生成结构化题目，请先根据讲解内容尝试作答。");
            fallback.put("extension", "请再说一说这首诗表达了怎样的情感。");
            return fallback;
        }
    }

    @SneakyThrows
    public void useFile(ChatClient.PromptSystemSpec spec, MultipartFile file) {
        if (file == null) return;
        String content = new TikaDocumentReader(new InputStreamResource(file.getInputStream())).get().get(0).getContent();
        Message message = new PromptTemplate("""
                已下内容是额外的知识，在你回答问题时可以参考下面的内容
                ---------------------
                {context}
                ---------------------
                """)
                .createMessage(Map.of("context", content));
        spec.text(message.getContent());
    }

}
