package io.github.qifan777.knowledge.demo;
import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.io.resource.Resource;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@RequestMapping("demo/document")
@RestController
@AllArgsConstructor
public class DocumentDemoController {
    private final DashScopeEmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    @PostMapping("embedding")
    public float[] embedding(@RequestParam String text) {
        return embeddingModel.embed(text);

    }

    @SneakyThrows
    @PostMapping("etl/reader/multipart")
    public String readForMultiPart(@RequestParam MultipartFile file) {
        Resource resource = new InputStreamResource(file.getInputStream());
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(String.valueOf(resource));
        return tikaDocumentReader.get().get(0).getContent();

    }

    @PostMapping("etl/reader/local-file")
    public String readFromLocalFile(@RequestParam String path) {
        org.springframework.core.io.Resource springResource = new FileSystemResource(path);
        return new TikaDocumentReader(springResource)
                .read()
                .get(0)
                .getContent();
    }
    @SneakyThrows
    @PostMapping("elt/write/vector")
    public void writeVector(@RequestParam MultipartFile file) {
        Resource resource = new InputStreamResource(file.getInputStream());

        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(String.valueOf(resource));
        List<Document> read = tikaDocumentReader.read();
        List<Document> split = new TokenTextSplitter().split(read);
        vectorStore.add(split);
    }
    /**
     * 查询向量数据库
     *
     * @param query 用户的提问
     * @return 匹配到的文档
     */

    @GetMapping("query")
    public List<Document> query(@RequestParam String query) {
        return vectorStore.similaritySearch(query);
    }

    private final ChatModel chatModel;
    @GetMapping(value = "chat/stream/database", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStreamWithDatabase(@RequestParam String prompt) {
        // 1. 定义提示词模板，question_answer_context会被替换成向量数据库中查询到的文档。
        String promptWithContext = """
                下面是上下文信息
                ---------------------
                {question_answer_context}
                ---------------------
                给定的上下文和提供的历史信息，而不是事先的知识，回复用户的意见。如果答案不在上下文中，告诉用户你不能回答这个问题。
                """;
        return ChatClient.create(chatModel).prompt()
                .user(prompt)
                // 2. QuestionAnswerAdvisor会在运行时替换模板中的占位符`question_answer_context`，替换成向量数据库中查询到的文档。此时的query=用户的提问+替换完的提示词模板;
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults(), promptWithContext))
                .stream()
                // 3. query发送给大模型得到答案
                .content()
                .map(chatResponse -> ServerSentEvent.builder(chatResponse)
                        .event("message")
                        .build());
    }

}
