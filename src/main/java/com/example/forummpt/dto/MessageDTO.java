package com.example.forummpt.dto;

import com.example.forummpt.models.Messages;

import java.sql.Timestamp;

public class MessageDTO {

    private long id;
    private String messageText;
    private Timestamp messageDatetime;
    private String username;
    private Long threadId;
    private Long userId;
    private Long repliedMessageId;

    public MessageDTO(Messages message) {
        this.id = message.getId();
        this.messageText = message.getMessageText();
        this.messageDatetime = message.getMessageDatetime();
        this.threadId = message.getThread().getId();
        this.userId = message.getMessageAuthor().getId();
        this.repliedMessageId = message.getMessageReply().getId();
        this.username = message.getMessageAuthor().getUsername();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Timestamp getMessageDatetime() {
        return messageDatetime;
    }

    public void setMessageDatetime(Timestamp messageDatetime) {
        this.messageDatetime = messageDatetime;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRepliedMessageId() {
        return repliedMessageId;
    }

    public void setRepliedMessageId(Long repliedMessageId) {
        this.repliedMessageId = repliedMessageId;
    }
}
