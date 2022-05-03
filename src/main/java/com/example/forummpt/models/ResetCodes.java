package com.example.forummpt.models;

import com.example.forummpt.dto.ResetCodeDTO;
import com.example.forummpt.repo.UsersRepo;

import javax.persistence.*;

@Entity
public class ResetCodes {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String resetCode;
    @OneToOne(optional = false)
    private User user;

    public ResetCodes() {
    }

    public ResetCodes(ResetCodeDTO dto, UsersRepo usersRepo) {
        this.id = dto.getId();
        this.resetCode = dto.getResetCode();
        this.user = usersRepo.searchById(dto.getUserId());
    }

    public ResetCodes(long id, String resetCode, User user) {
        this.id = id;
        this.resetCode = resetCode;
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResetCode() {
        return resetCode;
    }

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
