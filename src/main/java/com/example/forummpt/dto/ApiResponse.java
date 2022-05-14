package com.example.forummpt.dto;

public class ApiResponse {

    private String response;

    public ApiResponse() {
    }

    public ApiResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
