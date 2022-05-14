package com.example.forummpt.dto;

public class StatsDTO {

    private int totalMessagesCount;
    private int monthlyMessagesCount;
    private int totalThreadsCreated;
    private int monthlyThreadsCreated;

    public StatsDTO() {
    }

    public StatsDTO(int totalMessagesCount, int monthlyMessagesCount, int totalThreadsCreated, int monthlyThreadsCreated) {
        this.totalMessagesCount = totalMessagesCount;
        this.monthlyMessagesCount = monthlyMessagesCount;
        this.totalThreadsCreated = totalThreadsCreated;
        this.monthlyThreadsCreated = monthlyThreadsCreated;
    }

    public int getTotalMessagesCount() {
        return totalMessagesCount;
    }

    public void setTotalMessagesCount(int totalMessagesCount) {
        this.totalMessagesCount = totalMessagesCount;
    }

    public int getMonthlyMessagesCount() {
        return monthlyMessagesCount;
    }

    public void setMonthlyMessagesCount(int monthlyMessagesCount) {
        this.monthlyMessagesCount = monthlyMessagesCount;
    }

    public int getTotalThreadsCreated() {
        return totalThreadsCreated;
    }

    public void setTotalThreadsCreated(int totalThreadsCreated) {
        this.totalThreadsCreated = totalThreadsCreated;
    }

    public int getMonthlyThreadsCreated() {
        return monthlyThreadsCreated;
    }

    public void setMonthlyThreadsCreated(int monthlyThreadsCreated) {
        this.monthlyThreadsCreated = monthlyThreadsCreated;
    }
}
