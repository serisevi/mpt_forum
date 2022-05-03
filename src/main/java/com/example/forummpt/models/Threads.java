package com.example.forummpt.models;

import com.example.forummpt.dto.ThreadDTO;
import com.example.forummpt.repo.UsersRepo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.sql.Date;
import java.util.Set;

@Entity
public class Threads {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String threadName;
    private String threadDescription;
    private Date threadCreationTime;
    @OneToOne(optional = true)
    private User threadAuthor;
    @JsonIgnore
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL)
    private Set<Messages> threadMessages;
    @JsonIgnore
    @OneToMany(mappedBy = "banThread", cascade = CascadeType.ALL)
    private Set<LocalBanList> localBans;

    public Threads() {
    }

    public Threads(ThreadDTO dto, UsersRepo usersRepo) {
        this.id = dto.getId();
        this.threadName = dto.getThreadName();
        this.threadDescription = dto.getThreadDescription();
        this.threadCreationTime = dto.getThreadCreationTime();
        this.threadAuthor = usersRepo.searchById(dto.getUserId());
    }

    public Threads(long id, String threadName, String threadDescription, Date threadCreationTime, User threadAuthor, Set<Messages> threadMessages, Set<LocalBanList> localBans) {
        super();
        this.id = id;
        this.threadName = threadName;
        this.threadDescription = threadDescription;
        this.threadCreationTime = threadCreationTime;
        this.threadAuthor = threadAuthor;
        this.threadMessages = threadMessages;
        this.localBans = localBans;
    }

    public Set<Messages> getThreadMessages() {
        return threadMessages;
    }

    public void setThreadMessages(Set<Messages> threadMessages) {
        this.threadMessages = threadMessages;
    }

    public Set<LocalBanList> getLocalBans() {
        return localBans;
    }

    public void setLocalBans(Set<LocalBanList> localBans) {
        this.localBans = localBans;
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

    public User getThreadAuthor() {
        return threadAuthor;
    }

    public void setThreadAuthor(User threadAuthor) {
        this.threadAuthor = threadAuthor;
    }
}
