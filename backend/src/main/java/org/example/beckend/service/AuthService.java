package org.example.beckend.service;

import java.util.Optional;

import org.example.beckend.entity.UserInformation;
import org.example.beckend.repository.LoginAccountRepository;
import org.example.beckend.repository.UserInformationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final LoginAccountRepository loginAccountRepository;
    private final UserInformationRepository userInformationRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean allowLegacyPlaintext;

    public AuthService(
            LoginAccountRepository loginAccountRepository,
            UserInformationRepository userInformationRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.allow-legacy-plaintext:false}") boolean allowLegacyPlaintext) {
        this.loginAccountRepository = loginAccountRepository;
        this.userInformationRepository = userInformationRepository;
        this.passwordEncoder = passwordEncoder;
        this.allowLegacyPlaintext = allowLegacyPlaintext;
    }

    /**
     * 使用账号 ID 或邮箱之一登录；成功返回会话应绑定的用户 ID。
     */
    public Optional<Long> authenticate(Long id, String email, String password) {
        if (password == null || password.isBlank()) {
            return Optional.empty();
        }
        if (id != null) {
            return verifyLogin(id, password) ? Optional.of(id) : Optional.empty();
        }
        if (email != null && !email.isBlank()) {
            return userInformationRepository.findByUserEmailIgnoreCase(email.trim())
                    .filter(u -> verifyLogin(u.getId(), password))
                    .map(UserInformation::getId);
        }
        return Optional.empty();
    }

    /**
     * 校验登录：库中 {@code password} 应为 BCrypt 哈希（自带盐，存于密文字符串中）。
     * 若 {@code app.auth.allow-legacy-plaintext=true}，则在非 BCrypt 格式下回退为明文比对（仅用于迁移）。
     */
    public boolean verifyLogin(long id, String password) {
        if (password == null) {
            return false;
        }
        return loginAccountRepository.findById(id)
                .map(account -> verifyPassword(password, account.getPassword()))
                .orElse(false);
    }

    /**
     * 写入新密码或重置密码时调用，得到应存入库的 BCrypt 字符串。
     */
    public String encodePasswordForStorage(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    private boolean verifyPassword(String rawPassword, String stored) {
        if (stored == null || stored.isEmpty()) {
            return false;
        }
        if (isBcryptHash(stored)) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        return allowLegacyPlaintext && rawPassword.equals(stored);
    }

    private static boolean isBcryptHash(String stored) {
        return stored.startsWith("$2a$")
                || stored.startsWith("$2b$")
                || stored.startsWith("$2y$");
    }
}
