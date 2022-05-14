package com.example.forummpt.dto;

import com.example.forummpt.models.ApiSessions;
import com.example.forummpt.models.User;

import javax.persistence.*;

public class ApiSessionsDTO {
    private String id;
    private String userId;
    private String token;

    public ApiSessionsDTO() {
    }

    public ApiSessionsDTO(ApiSessions session) {
        this.id = String.valueOf(session.getId());
        this.userId = String.valueOf(session.getUser().getId());
        this.token = session.getToken();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
