package com.example.forummpt.models;

import com.example.forummpt.dto.LocalBanDTO;
import com.example.forummpt.repo.ThreadsRepo;
import com.example.forummpt.repo.UsersRepo;

import javax.persistence.*;
import java.sql.Date;

@Entity
public class LocalBanList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User bannedUser;
    @ManyToOne
    @JoinColumn(name = "thread_id", nullable = false)
    private Threads banThread;

    public LocalBanList() {
    }

    public LocalBanList(LocalBanDTO dto, UsersRepo usersRepo, ThreadsRepo threadsRepo) {
        this.id = dto.getId();
        this.bannedUser = usersRepo.searchById(dto.getUserId());
        this.banThread = threadsRepo.searchById(dto.getThreadId());
    }

    public LocalBanList(long id, User bannedUser, Threads banThread) {
        this.id = id;
        this.bannedUser = bannedUser;
        this.banThread = banThread;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getBannedUser() {
        return bannedUser;
    }

    public void setBannedUser(User bannedUser) {
        this.bannedUser = bannedUser;
    }

    public Threads getBanThread() {
        return banThread;
    }

    public void setBanThread(Threads banThread) {
        this.banThread = banThread;
    }
}
