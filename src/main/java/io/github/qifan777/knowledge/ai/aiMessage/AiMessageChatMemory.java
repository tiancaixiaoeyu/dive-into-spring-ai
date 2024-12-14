package io.github.qifan777.knowledge.ai.aiMessage;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.chat.memory.ChatMemory;

import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.model.Media;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.stereotype.Repository;
import org.springframework.util.MimeType;


import java.net.URL;
import java.util.HashMap;
import java.util.List;
@Repository
@AllArgsConstructor
public class AiMessageChatMemory implements ChatMemory {

       private final AiMessageRepository aiMessageRepository;



    @Override
    public void add(String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {

    }

    public List<Message> get(String conversationId, int lastN){

        return aiMessageRepository.findBySessionId(conversationId,lastN).stream().map(AiMessageChatMemory::toMessage).toList();

    }

    @Override
    public void clear(String conversationId) {
aiMessageRepository.deleteBySessionId(conversationId);
    }
    public  static Message toMessage (AiMessage aiMessage){
        List<Media> mediaList=aiMessage.medias().stream().map(AiMessageChatMemory::toMedia).toList();
        return new DefaultMessage(aiMessage.type(),aiMessage.textContent(),mediaList) ;
    }





    @SneakyThrows
    public static Media toMedia(AiMessage.Media media){
        return new Media(MimeType.valueOf(media.getType()), new URL(media.getData()));

    }
//    public DefaultMessage extends AbstractMessage{
//         public DefaultMessage(MessageType messageType, String content){
//            super(messageType,content);
//        }
//        public DefaultMessage(MessageType messageType, String textContent,List<Media> media){
//            super(messageType,textContent,media);
//        }
//    }
public static class DefaultMessage extends AbstractMessage {
    public DefaultMessage(MessageType messageType, String content) {
        super(messageType, content, new HashMap<>());
    }

    public DefaultMessage(MessageType messageType, String textContent, List<Media> media) {
        super(messageType, textContent, new HashMap<>());
        // 如果需要处理media，可以在metadata中添加
        if (media != null && !media.isEmpty()) {
            this.metadata.put("media", media);
        }
    }
}

}


