package com.example.forummpt.dto;

import com.example.forummpt.models.ResetCodes;

public class ResetCodeDTO {

    private long id;
    private String resetCode;
    private Long userId;

    public ResetCodeDTO(ResetCodes resetCode) {
        this.id = resetCode.getId();
        this.resetCode = resetCode.getResetCode();
        this.userId = resetCode.getUser().getId();
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
