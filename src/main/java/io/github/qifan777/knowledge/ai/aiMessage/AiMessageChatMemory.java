//package io.github.qifan777.knowledge.ai.aiMessage;
//
//import lombok.AllArgsConstructor;
//import lombok.SneakyThrows;
//import org.springframework.ai.chat.memory.ChatMemory;
//
//import org.springframework.ai.chat.messages.AbstractMessage;
//import org.springframework.ai.chat.messages.Message;
//import org.springframework.ai.chat.messages.MessageType;
//import org.springframework.ai.model.Media;
//import org.springframework.data.redis.connection.DefaultMessage;
//import org.springframework.stereotype.Repository;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.MimeType;
//
//
//import java.net.URL;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Repository
//@AllArgsConstructor
//public class AiMessageChatMemory implements ChatMemory {
//
//       private final AiMessageRepository aiMessageRepository;
//
//
//
//    @Override
//    public void add(String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {
//
//    }
//
//    public List<Message> get(String conversationId, int lastN){
//
//        return aiMessageRepository.findBySessionId(conversationId,lastN).stream().map(AiMessageChatMemory::toMessage).toList();
//
//
//    }
//
//    @Override
//    public void clear(String conversationId) {
//aiMessageRepository.deleteBySessionId(conversationId);
//    }
//    public  static Message toMessage (AiMessage aiMessage){
//        List<Media> mediaList= new ArrayList<>();
//        if (!CollectionUtils.isEmpty(aiMessage.medias())) {
//            mediaList= aiMessage.medias().stream().map(AiMessageChatMemory::toMedia).toList();}
//
//        return new DefaultMessage(aiMessage.type(),aiMessage.textContent(),mediaList) ;
//    }
//
//
//
//
//
//    @SneakyThrows
//    public static Media toMedia(AiMessage.Media media){
//        return new Media(MimeType.valueOf(media.getType()), new URL(media.getData()));
//
//    }
////    public DefaultMessage extends AbstractMessage{
////         public DefaultMessage(MessageType messageType, String content){
////            super(messageType,content);
////        }
////        public DefaultMessage(MessageType messageType, String textContent,List<Media> media){
////            super(messageType,textContent,media);
////        }
////    }
//public static class DefaultMessage extends AbstractMessage {
//    public DefaultMessage(MessageType messageType, String content) {
//        super(messageType, content, new HashMap<>());
//    }
//
//    public DefaultMessage(MessageType messageType, String textContent, List<Media> media) {
//        super(messageType, textContent, new HashMap<>());
//        // 如果需要处理media，可以在metadata中添加
//        if (media != null && !media.isEmpty()) {
//            this.metadata.put("media", media);
//        }
//    }
//}
//
//}
//
//
package io.github.qifan777.knowledge.ai.aiMessage;

import cn.hutool.core.collection.CollectionUtil;
import io.qifan.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.model.Media;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AiMessageChatMemory implements ChatMemory {
    private final AiMessageRepository messageRepository;

    /**
     * 不实现，手动前端发起请求保存用户的消息和大模型回复的消息
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
    }

    /**
     * 查询会话内的消息最新n条历史记录
     *
     * @param conversationId 会话id
     * @param lastN          最近n条
     * @return org.springframework.ai.chat.messages.Message格式的消息
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        return messageRepository
                // 查询会话内的最新n条消息
                .findBySessionId(conversationId, lastN)
                .stream()
                // 转成Message对象
                .map(AiMessageChatMemory::toSpringAiMessage)
                .toList();
    }

    /**
     * 清除会话内的消息
     *
     * @param conversationId 会话id
     */
    @Override
    public void clear(String conversationId) {
        messageRepository.deleteBySessionId(conversationId);
    }

    public static AiMessage toAiMessage(Message message, String sessionId) {
        return AiMessageDraft.$.produce(draft -> {
            draft.setSessionId(sessionId)
                    .setTextContent(message.getContent())
                    .setType(message.getMessageType())
                    .setMedias(new ArrayList<>());
            if (message instanceof UserMessage userMessage &&
                    !CollectionUtil.isEmpty(userMessage.getMedia())) {
                List<AiMessage.Media> mediaList = ((UserMessage) message)
                        .getMedia()
                        .stream()
//                        .map(media -> new AiMessage.Media()
//                                .setType(media.getMimeType().getType())
//                                .setData(media.getData().toString()))
//                        .toList();
                        .map(media -> new AiMessage.Media(
                                media.getMimeType().getType(),
                                media.getData().toString()))
                        .toList();
                draft.setMedias(mediaList);
            }
        });
    }

    public static Message toSpringAiMessage(AiMessage aiMessage) {
        List<Media> mediaList = new ArrayList<>();
        if (!CollectionUtil.isEmpty(aiMessage.medias())) {
            mediaList = aiMessage.medias().stream().map(AiMessageChatMemory::toMedia).toList();
        }
        if (aiMessage.type().equals(MessageType.ASSISTANT)) {
            return new AssistantMessage(aiMessage.textContent());
        }
        if (aiMessage.type().equals(MessageType.USER)) {
            return new UserMessage(aiMessage.textContent(), mediaList);
        }
        if (aiMessage.type().equals(MessageType.SYSTEM)) {
            return new SystemMessage(aiMessage.textContent());
        }
        throw new BusinessException("不支持的消息类型");
    }

    @SneakyThrows
    public static Media toMedia(AiMessage.Media media) {
        return new Media(new MediaType(media.getType()), new URL(media.getData()));
    }
}

