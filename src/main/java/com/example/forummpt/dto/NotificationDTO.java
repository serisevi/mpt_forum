package com.example.forummpt.dto;

import com.example.forummpt.models.Notifications;

public class NotificationDTO {

    private long id;
    private String text;
    private boolean notificationRead;
    private Long userId;
    private Long messageId;

    public NotificationDTO(Notifications notification) {
        this.id = notification.getId();
        this.text = notification.getText();
        this.notificationRead = notification.isNotificationRead();
        this.userId = notification.getUser().getId();
        this.messageId = notification.getMessage().getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isNotificationRead() {
        return notificationRead;
    }

    public void setNotificationRead(boolean notificationRead) {
        this.notificationRead = notificationRead;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
