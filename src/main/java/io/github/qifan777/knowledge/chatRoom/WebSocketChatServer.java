package io.github.qifan777.knowledge.chatRoom;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.yangziying.knowledge.chatRoom.ChatMessage;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/ws/chat/room/{roomId}")
@Component
public class WebSocketChatServer {
    private static final Map<String, Map<String, Session>> roomSessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("roomId") String roomId) {
        try {
            String userId = session.getRequestParameterMap().get("userId").get(0);
            if (userId == null) {
                log.error("用户ID为空，无法连接到房间 {}", roomId);
                return; // 退出方法
            }
            log.info("用户 {} 正在连接到房间 {}", userId, roomId);
            
            roomSessions.putIfAbsent(roomId, new ConcurrentHashMap<>());
            roomSessions.get(roomId).put(userId, session);
            
            ChatMessage joinMessage = new ChatMessage(
                UUID.randomUUID().toString(),
                userId,
                userId,
                "加入了聊天室",
                LocalDateTime.now().toString(),
                ChatMessage.MessageType.JOIN
            );
            broadcastToRoom(roomId, joinMessage);
              // 广播在线人数
        broadcastOnlineCount(roomId);
        } catch (Exception e) {
            log.error("WebSocket连接错误", e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("roomId") String roomId) {
        String userId = session.getRequestParameterMap().get("userId").get(0);
        ChatMessage chatMessage = new ChatMessage(
                UUID.randomUUID().toString(),
                userId,
                userId,
                message,
                LocalDateTime.now().toString(),
                ChatMessage.MessageType.CHAT
        );
        broadcastToRoom(roomId, chatMessage);
    }

    @OnClose
    public void onClose(Session session, @PathParam("roomId") String roomId) {
        String userId = session.getRequestParameterMap().get("userId").get(0);
        if (roomSessions.containsKey(roomId)) {
            roomSessions.get(roomId).remove(userId);
            broadcastToRoom(roomId, new ChatMessage(
                    UUID.randomUUID().toString(),
                    userId,
                    userId,
                    "离开了聊天室",
                    LocalDateTime.now().toString(),
                    ChatMessage.MessageType.LEAVE
            ));
            // 广播在线人数
            broadcastOnlineCount(roomId);
        }
    }
    // 在现有代码基础上添加广播在线人数的方法
    private void broadcastOnlineCount(String roomId) {
        try {
            Map<String, Session> roomUsers = roomSessions.get(roomId);
            int onlineCount = roomUsers != null ? roomUsers.size() : 0;
            
            ChatMessage countMessage = new ChatMessage(
                UUID.randomUUID().toString(),
                "system",
                "system",
                String.valueOf(onlineCount),
                LocalDateTime.now().toString(),
                ChatMessage.MessageType.ONLINE_COUNT
            );
            
            broadcastToRoom(roomId, countMessage);
        } catch (Exception e) {
            log.error("广播在线人数时发生错误", e);
        }
    }

    private void broadcastToRoom(String roomId, ChatMessage message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            Map<String, Session> roomUsers = roomSessions.get(roomId);
            if (roomUsers != null) {
                for (Session userSession : roomUsers.values()) {
                    synchronized (userSession) {
                        userSession.getBasicRemote().sendText(messageJson);
                    }
                }
            }
        } catch (Exception e) {
            log.error("广播消息时发生错误", e);
        }
    }
}