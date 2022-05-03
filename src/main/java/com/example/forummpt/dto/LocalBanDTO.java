package com.example.forummpt.dto;

import com.example.forummpt.models.LocalBanList;

public class LocalBanDTO {

    private long id;
    private Long userId;
    private Long threadId;

    public LocalBanDTO(LocalBanList localBanList) {
        this.id = localBanList.getId();
        this.userId = localBanList.getBannedUser().getId();
        this.threadId = localBanList.getBanThread().getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }
}
