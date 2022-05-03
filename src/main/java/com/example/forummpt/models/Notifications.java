package com.example.forummpt.models;

import com.example.forummpt.dto.NotificationDTO;
import com.example.forummpt.repo.MessagesRepo;
import com.example.forummpt.repo.UsersRepo;

import javax.persistence.*;

@Entity
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String text;
    private boolean notificationRead;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToOne(optional = true)
    private Messages message;

    public Notifications() {
    }

    public Notifications(NotificationDTO dto, UsersRepo usersRepo, MessagesRepo messagesRepo) {
        this.id = dto.getId();
        this.text = dto.getText();
        this.user = usersRepo.searchById(dto.getUserId());
        this.message = messagesRepo.searchById(dto.getMessageId());
    }

    public Notifications(long id, String text, boolean notificationRead, User user, Messages message) {
        this.id = id;
        this.text = text;
        this.notificationRead = notificationRead;
        this.user = user;
        this.message = message;
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

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Messages getMessage() {
        return message;
    }
    public void setMessage(Messages message) {
        this.message = message;
    }

    public boolean isNotificationRead() {
        return notificationRead;
    }
    public void setNotificationRead(boolean notificationRead) {
        this.notificationRead = notificationRead;
    }
}
