package com.artgallery.services;

import com.artgallery.domain.Notification;
import com.artgallery.domain.User;
import com.artgallery.repositories.NotificationRepository;
import com.artgallery.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification createNotification(UUID recipientId, UUID actorId, String type, UUID targetId, String targetModel, String contentPreview) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        User actor = null;
        if (actorId != null) {
            actor = userRepository.findById(actorId).orElse(null);
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .targetId(targetId)
                .targetModel(targetModel)
                .contentPreview(contentPreview)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);

        // Phát sóng (broadcast) thông báo tới người nhận qua WebSocket
        // Sử dụng chuỗi ID người nhận làm tên principal cho kết nối WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                    recipientId.toString(),
                    "/queue/notifications",
                    notification
            );
            System.out.println("[NotificationService] Broadcasted notification to: " + recipientId);
        } catch (Exception e) {
            System.err.println("[NotificationService] Failed to broadcast WebSocket: " + e.getMessage());
        }

        return notification;
    }
}
