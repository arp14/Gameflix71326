package com.gameflix.dto;

public class LoginResponse {

    private boolean success;
    private Long userId;
    private String username;
    private String message;
    private String token;

    public static LoginResponse success(Long userId, String username, String token) {
        LoginResponse response = new LoginResponse();
        response.success = true;
        response.userId = userId;
        response.username = username;
        response.message = "Login successful";
        response.token = token;
        return response;
    }

    public static LoginResponse failure(String message) {
        LoginResponse response = new LoginResponse();
        response.success = false;
        response.message = message;
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}
