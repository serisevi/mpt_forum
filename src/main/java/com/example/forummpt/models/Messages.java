package com.example.forummpt.models;

import com.example.forummpt.dto.MessageDTO;
import com.example.forummpt.repo.MessagesRepo;
import com.example.forummpt.repo.ThreadsRepo;
import com.example.forummpt.repo.UsersRepo;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Array;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String messageText;
    private Timestamp messageDatetime;
    @ManyToOne
    @JoinColumn(name = "thread_id", nullable = false)
    private Threads thread;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User messageAuthor;
    @JsonIgnore
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private Set<MessageImages> messageImages;
    @OneToOne(optional = true)
    private Messages messageReply;

    public Messages() {
    }

    public Messages(MessageDTO dto, ThreadsRepo threadsRepo, UsersRepo usersRepo, MessagesRepo messagesRepo) {
        this.id = dto.getId();
        this.messageText = dto.getMessageText();
        this.messageDatetime = dto.getMessageDatetime();
        this.thread = threadsRepo.searchById(dto.getThreadId());
        this.messageAuthor = usersRepo.searchById(dto.getUserId());
        this.messageReply = messagesRepo.searchById(dto.getRepliedMessageId());
    }

    public Messages(long id, String messageText, Timestamp messageDatetime, Threads thread, User messageAuthor, Set<MessageImages> messageImages, Messages messageReply) {
        this.id = id;
        this.messageText = messageText;
        this.messageDatetime = messageDatetime;
        this.thread = thread;
        this.messageAuthor = messageAuthor;
        this.messageImages = messageImages;
        this.messageReply = messageReply;
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

    public Threads getThread() {
        return thread;
    }

    public void setThread(Threads thread) {
        this.thread = thread;
    }

    public User getMessageAuthor() {
        return messageAuthor;
    }

    public void setMessageAuthor(User messageAuthor) {
        this.messageAuthor = messageAuthor;
    }

    public Set<MessageImages> getMessageImages() {
        return messageImages;
    }

    public void setMessageImages(Set<MessageImages> messageImages) {
        this.messageImages = messageImages;
    }

    public Messages getMessageReply() {
        return messageReply;
    }

    public void setMessageReply(Messages messageReply) {
        this.messageReply = messageReply;
    }
}
