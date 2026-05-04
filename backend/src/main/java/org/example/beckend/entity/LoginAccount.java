package org.example.beckend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 映射表 {@code login}：{@code id} bigint PK，{@code password} text（存 BCrypt 哈希，非明文）。
 */
@Entity
@Table(name = "login")
public class LoginAccount {

    @Id
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
