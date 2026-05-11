package org.example.beckend.service;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.example.beckend.dto.RegisterResponse;
import org.example.beckend.entity.LoginAccount;
import org.example.beckend.entity.UserInformation;
import org.example.beckend.repository.LoginAccountRepository;
import org.example.beckend.repository.UserInformationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.example.beckend.dto.LoginResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private static final long MIN_NINE_DIGIT_ID = 100_000_000L;
    private static final long MAX_NINE_DIGIT_EXCLUSIVE = 1_000_000_000L;
    private static final int MAX_ID_ATTEMPTS = 64;
    private static final int USER_NAME_MAX_LEN = 20;
    private static final int PASSWORD_MIN_LEN = 6;
    private static final int PASSWORD_MAX_LEN = 72;

    /** 与前端格式校验思路一致：非空白、含 @ 与域名点段 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserInformationRepository userInformationRepository;
    private final LoginAccountRepository loginAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserInformationRepository userInformationRepository,
            LoginAccountRepository loginAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.userInformationRepository = userInformationRepository;
        this.loginAccountRepository = loginAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(String rawUserName, String rawEmail, String rawPassword) {
        String userNameError = validateUserName(rawUserName);
        if (userNameError != null) {
            return RegisterResponse.fail(userNameError);
        }
        String emailError = validateEmail(rawEmail);
        if (emailError != null) {
            return RegisterResponse.fail(emailError);
        }
        String passwordError = validatePassword(rawPassword);
        if (passwordError != null) {
            return RegisterResponse.fail(passwordError);
        }

        String userName = rawUserName.trim();
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        String password = rawPassword;

        if (userInformationRepository.existsByUserEmailIgnoreCase(email)) {
            return RegisterResponse.fail("该邮箱已注册，一个邮箱只能绑定一个账号");
        }

        for (int attempt = 0; attempt < MAX_ID_ATTEMPTS; attempt++) {
            long candidateId = ThreadLocalRandom.current().nextLong(MIN_NINE_DIGIT_ID, MAX_NINE_DIGIT_EXCLUSIVE);
            if (userInformationRepository.existsById(candidateId) || loginAccountRepository.existsById(candidateId)) {
                continue;
            }
            try {
                UserInformation profile = new UserInformation();
                profile.setId(candidateId);
                profile.setUserName(userName);
                profile.setUserEmail(email);
                userInformationRepository.save(profile);

                LoginAccount login = new LoginAccount();
                login.setId(candidateId);
                login.setPassword(passwordEncoder.encode(password));
                loginAccountRepository.save(login);

                return RegisterResponse.ok(candidateId);
            } catch (DataIntegrityViolationException e) {
                if (userInformationRepository.existsByUserEmailIgnoreCase(email)) {
                    return RegisterResponse.fail("该邮箱已注册，一个邮箱只能绑定一个账号");
                }
                // 主键冲突等，换号重试
            }
        }
        return RegisterResponse.fail("暂时无法分配唯一账号，请稍后重试");
    }

    /**
     * 修改当前登录用户的基本资料（用户名、邮箱）；邮箱全局唯一（排除本人）。
     */
    @Transactional
    public LoginResponse updateProfile(long userId, String rawUserName, String rawEmail) {
        String userNameError = validateUserName(rawUserName);
        if (userNameError != null) {
            return new LoginResponse(false, userNameError);
        }
        String emailError = validateEmail(rawEmail);
        if (emailError != null) {
            return new LoginResponse(false, emailError);
        }
        String userName = rawUserName.trim();
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);

        UserInformation existing = userInformationRepository.findById(userId).orElse(null);
        if (existing == null) {
            return new LoginResponse(false, "用户不存在");
        }
        if (!existing.getUserEmail().equalsIgnoreCase(email)) {
            var other = userInformationRepository.findByUserEmailIgnoreCase(email);
            if (other.isPresent() && !other.get().getId().equals(userId)) {
                return new LoginResponse(false, "该邮箱已被其他账号使用");
            }
        }
        existing.setUserName(userName);
        existing.setUserEmail(email);
        userInformationRepository.save(existing);
        return new LoginResponse(true, "资料已更新");
    }

    private static String validateUserName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "请填写用户名";
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return "请填写用户名";
        }
        if (t.length() > USER_NAME_MAX_LEN) {
            return "用户名长度不能超过 " + USER_NAME_MAX_LEN + " 个字符";
        }
        return null;
    }

    private static String validateEmail(String raw) {
        if (raw == null || raw.isBlank()) {
            return "请填写邮箱";
        }
        String email = raw.trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "邮箱格式不正确";
        }
        return null;
    }

    private static String validatePassword(String raw) {
        if (raw == null || raw.isBlank()) {
            return "请填写密码";
        }
        if (raw.length() < PASSWORD_MIN_LEN) {
            return "密码至少 " + PASSWORD_MIN_LEN + " 位";
        }
        if (raw.length() > PASSWORD_MAX_LEN) {
            return "密码过长";
        }
        return null;
    }
}
