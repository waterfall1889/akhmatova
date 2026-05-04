package org.example.beckend.controller;

import org.example.beckend.auth.AuthSessionConstants;
import org.example.beckend.dto.LoginRequest;
import org.example.beckend.dto.LoginResponse;
import org.example.beckend.dto.RegisterRequest;
import org.example.beckend.dto.RegisterResponse;
import org.example.beckend.dto.SessionUserResponse;
import org.example.beckend.service.AuthService;
import org.example.beckend.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;

    public AuthController(AuthService authService, RegistrationService registrationService) {
        this.authService = authService;
        this.registrationService = registrationService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest body) {
        RegisterResponse result = registrationService.register(body.userName(), body.userEmail(), body.password());
        if (!result.success()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest body, HttpServletRequest request) {
        if (body.password() == null || body.password().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(false, "请填写密码"));
        }
        boolean byId = body.id() != null;
        boolean byEmail = body.email() != null && !body.email().isBlank();
        if (byId == byEmail) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse(false, "请使用账号 ID 或邮箱其中一种方式登录"));
        }
        var userId = authService.authenticate(body.id(), body.email(), body.password());
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "账号或密码错误"));
        }
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthSessionConstants.USER_ID, userId.get());
        return ResponseEntity.ok(new LoginResponse(true, "登录成功"));
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(new LoginResponse(true, "已退出"));
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SessionUserResponse> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Object raw = session.getAttribute(AuthSessionConstants.USER_ID);
        if (!(raw instanceof Number num)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new SessionUserResponse(num.longValue()));
    }
}
