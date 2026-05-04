package org.example.beckend.dto;

public record RegisterResponse(boolean success, String message, Long id) {

    public static RegisterResponse ok(long id) {
        return new RegisterResponse(true, "注册成功", id);
    }

    public static RegisterResponse fail(String message) {
        return new RegisterResponse(false, message, null);
    }
}
