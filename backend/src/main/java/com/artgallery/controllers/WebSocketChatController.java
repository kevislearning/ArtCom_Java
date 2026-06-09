package com.artgallery.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/typing")
    public void handleTypingStatus(Principal principal, Map<String, Object> payload) {
        if (principal == null) return;

        String senderId = principal.getName();
        String receiverId = (String) payload.get("receiverId");
        Boolean isTyping = (Boolean) payload.get("isTyping");

        if (receiverId != null) {
            Map<String, Object> output = new HashMap<>();
            output.put("senderId", senderId);
            output.put("isTyping", isTyping);

            // Chuyển tiếp trạng thái đang gõ chữ tới hàng đợi (queue) của người nhận
            messagingTemplate.convertAndSendToUser(receiverId, "/queue/typing", output);
            System.out.println("[WebSocketChatController] Forwarded typing status from " + senderId + " to " + receiverId);
        }
    }
}
