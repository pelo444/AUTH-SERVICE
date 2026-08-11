package com.mztarou.auth_service.controller;

import com.mztarou.auth_service.entity.User;
import com.mztarou.auth_service.repository.SsoAppRepository;
import com.mztarou.auth_service.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final SsoAppRepository ssoAppRepository;

    public AuthController(AuthenticationManager authenticationManager,
                        UserService userService,
                        SsoAppRepository ssoAppRepository) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.ssoAppRepository = ssoAppRepository;
    }

    // ===========================
    // ログイン
    // ===========================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        String personId = body.get("personId");
        String password = body.get("password");

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(personId, password)
            );

            // Spring Securityのセッションにセキュリティコンテキストを保存
            HttpSession session = request.getSession(true);
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(auth);
            SecurityContextHolder.setContext(securityContext);
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
            );
            session.setAttribute("personId", personId);

            Optional<User> userOpt = userService.findActiveUser(personId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(401)
                    .body(Map.of("message", "ユーザーが見つかりません"));
            }

            Cookie cookie = new Cookie("LOGGED_IN", "true");
            cookie.setHttpOnly(false);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 8);
            response.addCookie(cookie);

            // SSOアプリリストを取得
            List<Map<String, String>> ssoApps = ssoAppRepository
                .findActiveAppsByPersonId(personId)
                .stream()
                .map(app -> Map.of(
                    "appName", app.getAppName(),
                    "appUrl", app.getAppUrl(),
                    "description", app.getDescription() != null ? app.getDescription() : ""
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "message", "ログインしました",
                "username", userOpt.get().getUsername(),
                "personId", personId,
                "ssoApps", ssoApps
            ));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                .body(Map.of("message", "認証に失敗しました"));
        }
    }

    // ===========================
    // ログアウト
    // ===========================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        // セキュリティコンテキストをクリア
        SecurityContextHolder.clearContext();

        // セッションを破棄
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Cookieを削除
        Cookie cookie = new Cookie("LOGGED_IN", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "ログアウトしました"));
    }

    // ===========================
    // ログイン状態確認
    // ===========================
    @GetMapping("/status")
    public ResponseEntity<?> status(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("personId") == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }

        return ResponseEntity.ok(Map.of(
            "loggedIn", true,
            "personId", session.getAttribute("personId")
        ));
    }

    // ===========================
    // SSOアプリ一覧取得
    // ===========================
    @GetMapping("/sso-apps")
    public ResponseEntity<?> getSsoApps(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("personId") == null) {
            return ResponseEntity.status(401)
                .body(Map.of("message", "ログインが必要です"));
        }

        String personId = (String) session.getAttribute("personId");
        List<Map<String, String>> ssoApps = ssoAppRepository
            .findActiveAppsByPersonId(personId)
            .stream()
            .map(app -> Map.of(
                "appName", app.getAppName(),
                "appUrl", app.getAppUrl(),
                "description", app.getDescription() != null ? app.getDescription() : ""
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("ssoApps", ssoApps));
    }
}