package com.example.forummpt.models;

import com.example.forummpt.dto.GlobalBanDTO;
import com.example.forummpt.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.sql.Date;

@Entity
public class GlobalBanList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private boolean loginBan;
    private boolean writeBan;
    private Date banStartDate;
    private Date banExpireDate;
    @OneToOne(optional = false)
    private User bannedUser;

    public GlobalBanList() {
    }

    public GlobalBanList(long id, boolean loginBan, boolean writeBan, Date banStartDate, Date banExpireDate, User bannedUser) {
        this.id = id;
        this.loginBan = loginBan;
        this.writeBan = writeBan;
        this.banStartDate = banStartDate;
        this.banExpireDate = banExpireDate;
        this.bannedUser = bannedUser;
    }

    public GlobalBanList(GlobalBanDTO dto, UsersRepo usersRepo) {
        this.id = dto.getId();
        this.loginBan = dto.isLoginBan();
        this.writeBan = dto.isWriteBan();
        this.banStartDate = dto.getBanStartDate();
        this.banExpireDate = dto.getBanExpireDate();
        this.bannedUser = usersRepo.searchById(dto.getUserId());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isLoginBan() {
        return loginBan;
    }

    public void setLoginBan(boolean loginBan) {
        this.loginBan = loginBan;
    }

    public boolean isWriteBan() {
        return writeBan;
    }

    public void setWriteBan(boolean writeBan) {
        this.writeBan = writeBan;
    }

    public Date getBanStartDate() {
        return banStartDate;
    }

    public void setBanStartDate(Date banStartDate) {
        this.banStartDate = banStartDate;
    }

    public Date getBanExpireDate() {
        return banExpireDate;
    }

    public void setBanExpireDate(Date banExpireDate) {
        this.banExpireDate = banExpireDate;
    }

    public User getBannedUser() {
        return bannedUser;
    }

    public void setBannedUser(User bannedUser) {
        this.bannedUser = bannedUser;
    }
}
