package org.example.beckend.dto;

/**
 * @param id    与 {@code email} 二选一：使用账号 ID 登录
 * @param email 与 {@code id} 二选一：使用邮箱登录
 */
public record LoginRequest(Long id, String email, String password) {
}
