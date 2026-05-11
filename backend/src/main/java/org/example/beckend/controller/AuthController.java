package org.example.beckend.controller;

import org.example.beckend.auth.AuthSessionConstants;
import org.example.beckend.dto.LoginRequest;
import org.example.beckend.dto.LoginResponse;
import java.util.Optional;

import org.example.beckend.dto.ProfileUpdateRequest;
import org.example.beckend.dto.RegisterRequest;
import org.example.beckend.dto.RegisterResponse;
import org.example.beckend.dto.SessionUserResponse;
import org.example.beckend.mongo.UserAvatarDocument;
import org.example.beckend.mongo.UserAvatarMongoRepository;
import org.example.beckend.repository.UserInformationRepository;
import org.example.beckend.service.AuthService;
import org.example.beckend.service.RegistrationService;
import org.example.beckend.service.UserAvatarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final UserInformationRepository userInformationRepository;
    private final UserAvatarMongoRepository userAvatarMongoRepository;
    private final UserAvatarService userAvatarService;

    public AuthController(
            AuthService authService,
            RegistrationService registrationService,
            UserInformationRepository userInformationRepository,
            UserAvatarMongoRepository userAvatarMongoRepository,
            UserAvatarService userAvatarService) {
        this.authService = authService;
        this.registrationService = registrationService;
        this.userInformationRepository = userInformationRepository;
        this.userAvatarMongoRepository = userAvatarMongoRepository;
        this.userAvatarService = userAvatarService;
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
        return currentUserId(request)
                .flatMap(userInformationRepository::findById)
                .map(u -> new SessionUserResponse(u.getId(), u.getUserName(), u.getUserEmail()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> updateProfile(@RequestBody ProfileUpdateRequest body, HttpServletRequest request) {
        Optional<Long> uid = currentUserId(request);
        if (uid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LoginResponse result = registrationService.updateProfile(uid.get(), body.userName(), body.userEmail());
        if (!result.success()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> uploadAvatar(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        Optional<Long> uid = currentUserId(request);
        if (uid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LoginResponse result = userAvatarService.saveAvatar(uid.get(), file);
        if (!result.success()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 当前用户自定义头像（MongoDB）；无记录或空数据时返回 404，前端使用默认字头像。
     */
    @GetMapping(value = "/avatar")
    public ResponseEntity<byte[]> avatar(HttpServletRequest request) {
        Optional<Long> uid = currentUserId(request);
        if (uid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userIdKey = Long.toString(uid.get());
        return userAvatarMongoRepository.findById(userIdKey)
                .filter(this::hasImageBytes)
                .map(this::toImageResponse)
                .orElse(ResponseEntity.notFound().build());
    }

    private boolean hasImageBytes(UserAvatarDocument doc) {
        byte[] img = doc.getImage();
        return img != null && img.length > 0;
    }

    private ResponseEntity<byte[]> toImageResponse(UserAvatarDocument doc) {
        String ct = doc.getContentType();
        if (ct == null || ct.isBlank()) {
            ct = MediaType.IMAGE_JPEG_VALUE;
        }
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(ct);
        } catch (Exception e) {
            mediaType = MediaType.IMAGE_JPEG;
        }
        return ResponseEntity.ok().contentType(mediaType).body(doc.getImage());
    }

    private static Optional<Long> currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object raw = session.getAttribute(AuthSessionConstants.USER_ID);
        if (raw instanceof Number num) {
            return Optional.of(num.longValue());
        }
        return Optional.empty();
    }
}
