package com.example.forummpt.dto;

import com.example.forummpt.models.Threads;
import java.sql.Date;

public class ThreadDTO {

    private long id;
    private String threadName;
    private String threadDescription;
    private Date threadCreationTime;
    private Long userId;
    private String username;
    private String avatar;

    public ThreadDTO() {}

    public ThreadDTO(Threads thread) {
        this.id = thread.getId();
        this.threadName = thread.getThreadName();
        this.threadDescription = thread.getThreadDescription();
        this.threadCreationTime = thread.getThreadCreationTime();
        this.userId = thread.getThreadAuthor().getId();
        this.username = thread.getThreadAuthor().getUsername();
        this.avatar = thread.getThreadAuthor().getUserInfo().getImageUrl();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadDescription() {
        return threadDescription;
    }

    public void setThreadDescription(String threadDescription) {
        this.threadDescription = threadDescription;
    }

    public Date getThreadCreationTime() {
        return threadCreationTime;
    }

    public void setThreadCreationTime(Date threadCreationTime) {
        this.threadCreationTime = threadCreationTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
