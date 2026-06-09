package com.artgallery.controllers;

import com.artgallery.domain.Message;
import com.artgallery.domain.User;
import com.artgallery.repositories.MessageRepository;
import com.artgallery.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @Transactional
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal User authUser,
            @RequestBody Map<String, String> body) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        String receiverIdStr = body.get("receiverId");
        String content = body.get("content");

        if (receiverIdStr == null || content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Receiver and content are required"));
        }

        UUID receiverId = UUID.fromString(receiverIdStr);
        UUID senderId = authUser.getId();

        Optional<User> receiverOpt = userRepository.findById(receiverId);
        if (receiverOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Recipient not found"));
        }

        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = receiverOpt.get();

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content.trim())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        // Phát sóng tới người nhận qua hàng đợi người dùng (user queue) Spring STOMP WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/messages",
                    message
            );
            System.out.println("[MessageController] Dispatched WS message to: " + receiverId);
        } catch (Exception e) {
            System.err.println("[MessageController] Failed to dispatch WS message: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/{userId}")
    @Transactional
    public ResponseEntity<?> getMessages(
            @AuthenticationPrincipal User authUser,
            @PathVariable("userId") UUID otherUserId) {
        
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        UUID userId = authUser.getId();

        // Lấy lịch sử tin nhắn giữa hai người dùng này
        List<Message> messages = messageRepository.findChatHistory(userId, otherUserId);

        // Đánh dấu các tin nhắn gửi đến là đã đọc
        messageRepository.markAsRead(otherUserId, userId);

        return ResponseEntity.ok(messages);
    }

    public static class ConversationItem {
        public User user;
        public Message lastMessage;
        public long unreadCount;

        public ConversationItem(User user, Message lastMessage, long unreadCount) {
            this.user = user;
            this.lastMessage = lastMessage;
            this.unreadCount = unreadCount;
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(@AuthenticationPrincipal User authUser) {
        if (authUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        UUID userId = authUser.getId();

        // Lấy danh sách tin nhắn được sắp xếp giảm dần theo thời gian tạo (createdAt DESC)
        List<Message> messages = messageRepository.findConversationsForUser(userId);

        Map<UUID, ConversationItem> conversationMap = new LinkedHashMap<>();

        for (Message msg : messages) {
            User sender = msg.getSender();
            User receiver = msg.getReceiver();
            if (sender == null || receiver == null) continue;

            boolean isSenderMe = sender.getId().equals(userId);
            User otherUser = isSenderMe ? receiver : sender;

            if (!conversationMap.containsKey(otherUser.getId())) {
                long unreadCount = messageRepository.countBySenderIdAndReceiverIdAndIsReadFalse(otherUser.getId(), userId);
                conversationMap.put(otherUser.getId(), new ConversationItem(otherUser, msg, unreadCount));
            }
        }

        return ResponseEntity.ok(new ArrayList<>(conversationMap.values()));
    }
}
