package com.example.forummpt.models;

import javax.persistence.*;

@Entity
public class ApiSessions {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @OneToOne(optional = false)
    private User user;
    private String token;

    public ApiSessions() {}

    public ApiSessions(long id, User user, String token) {
        this.id = id;
        this.user = user;
        this.token = token;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}
