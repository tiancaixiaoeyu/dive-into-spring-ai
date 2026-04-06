package io.github.qifan777.knowledge.demo;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequestMapping("demo/message")
@RestController
@AllArgsConstructor
public class MessageDemoController {

    private final ChatModel chatModel;
    private final ChatMemory chatMemory = new InMemoryChatMemory();

    @GetMapping("chat")
    public String chat(@RequestParam String prompt) {
        ChatClient chatClient = ChatClient.create(chatModel);
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @GetMapping(value = "chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String prompt) {
        return ChatClient.create(chatModel)
                .prompt()
                .user(prompt)
                .stream()
                .content();
    }

    @GetMapping(value = "chat/stream/history", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamWithHistory(@RequestParam String prompt,
                                              @RequestParam String sessionId) {
        MessageChatMemoryAdvisor messageChatMemoryAdvisor =
                new MessageChatMemoryAdvisor(chatMemory, sessionId, 10);
        return ChatClient.create(chatModel)
                .prompt()
                .user(prompt)
                .advisors(messageChatMemoryAdvisor)
                .stream()
                .content();
    }

    @GetMapping(value = "chat/stream/function", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamWithFunction(@RequestParam String prompt) {
        return ChatClient.create(chatModel)
                .prompt()
                .user(prompt)
                .functions("documentAnalyzeFunction")
                .stream()
                .content();
    }
}
