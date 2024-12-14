package io.github.qifan777.knowledge.ai.aiMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.qifan777.knowledge.ai.aiMessage.dto.AiMessageInput;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.Map;

@RequestMapping("message")
@RestController
@AllArgsConstructor
@Slf4j
public class AiMessageController{
  private final AiMessageRepository aiMessageRepository;
  private final AiMessageChatMemory chatMemory;
  private final ChatModel chatModel;


    @PostMapping
    public void  save(@RequestBody AiMessageInput input){
        aiMessageRepository.save(input.toEntity());

    }
    @PostMapping(value = "chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody AiMessageInput input){
        var  advisor =new MessageChatMemoryAdvisor(chatMemory,input.getSessionId(),10);
        return ChatClient.create(chatModel).prompt()
                .user(promptUserSpec -> {
                    Message message = AiMessageChatMemory.toMessage(input.toEntity());
                    promptUserSpec.text(input.getTextContent());
                    if (!CollectionUtils.isEmpty(input.getMedias())) {
                        Media[] media = input.getMedias().toArray(new Media[0]);
                        promptUserSpec.media(media);
                    }
    }).advisors(advisor)
                .stream()
                .content()
                .map(content -> ServerSentEvent.<String>builder(content)
                    .event("message").build());
            }


}
