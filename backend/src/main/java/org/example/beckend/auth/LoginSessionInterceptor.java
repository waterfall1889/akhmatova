package org.example.beckend.auth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 未登录访问受保护 /api/** 时：整页导航类请求 302 到前端登录页；XHR/fetch 返回 401 JSON（含 loginUrl）。
 */
@Component
public class LoginSessionInterceptor implements HandlerInterceptor {

    private final String frontendLoginUrl;

    public LoginSessionInterceptor(
            @Value("${app.auth.frontend-login-url:http://localhost:5173/login}") String frontendLoginUrl) {
        this.frontendLoginUrl = frontendLoginUrl;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthSessionConstants.USER_ID) != null) {
            return true;
        }
        if (preferBrowserRedirect(request)) {
            response.sendRedirect(frontendLoginUrl);
            return false;
        }
        writeUnauthorizedJson(response);
        return false;
    }

    /**
     * 典型整页 GET：{@code Sec-Fetch-Mode: navigate}；旧环境无该头时若 Accept 以 HTML 为主则重定向。
     */
    private static boolean preferBrowserRedirect(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String mode = request.getHeader("Sec-Fetch-Mode");
        if (mode != null) {
            return "navigate".equalsIgnoreCase(mode);
        }
        String accept = request.getHeader("Accept");
        if (accept == null) {
            return false;
        }
        return accept.contains("text/html") && !accept.startsWith("application/json");
    }

    private void writeUnauthorizedJson(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String loginJson = escapeJsonString(frontendLoginUrl);
        response.getWriter().write(
                "{\"success\":false,\"message\":\"未登录或会话已失效\",\"loginUrl\":\"" + loginJson + "\"}");
    }

    private static String escapeJsonString(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
