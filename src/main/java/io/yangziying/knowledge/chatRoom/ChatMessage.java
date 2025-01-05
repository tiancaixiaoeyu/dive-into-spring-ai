package io.yangziying.knowledge.chatRoom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String id;
    private String userId;
    private String username;
    private String content;
    private String timestamp;
    private MessageType type;
    private String nickname;
    
    public enum MessageType {
        CHAT,    // 聊天消息
        JOIN,    // 加入消息
        LEAVE,   // 离开消息
        ONLINE_COUNT // 在线人数
    }
}