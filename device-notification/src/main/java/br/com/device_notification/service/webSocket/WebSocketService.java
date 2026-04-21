package br.com.device_notification.service.webSocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyNewNotification(int count) {
        messagingTemplate.convertAndSend("/topic/notifications", count);
    }
}
