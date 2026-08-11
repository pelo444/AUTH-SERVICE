package com.mztarou.auth_service.controller;

import com.mztarou.auth_service.entity.User;
import com.mztarou.auth_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ===========================
    // 新規ユーザー登録
    // ===========================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String token = body.get("token");
            String username = body.get("username");
            String personId = body.get("personId");
            String password = body.get("password");

            // tokenを検証
            userService.verifyToken(token);

            // ユーザー登録
            User user = userService.registerUser(username, personId, password);

            // tokenを使用済みにする
            userService.markTokenAsUsed(token);

            return ResponseEntity.ok(Map.of(
                "message", "登録しました",
                "username", user.getUsername(),
                "personId", user.getPersonId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "登録に失敗しました: " + e.getMessage()));
        }
    }

    // ===========================
    // username変更
    // ===========================
    @PutMapping("/username")
    public ResponseEntity<?> changeUsername(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("personId") == null) {
            return ResponseEntity.status(401)
                .body(Map.of("message", "ログインが必要です"));
        }

        try {
            String personId = (String) session.getAttribute("personId");
            String newUsername = body.get("username");

            User user = userService.changeUsername(personId, newUsername);

            return ResponseEntity.ok(Map.of(
                "message", "usernameを変更しました",
                "username", user.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "変更に失敗しました: " + e.getMessage()));
        }
    }

    // ===========================
    // パスワード変更
    // ===========================
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("personId") == null) {
            return ResponseEntity.status(401)
                .body(Map.of("message", "ログインが必要です"));
        }

        try {
            String personId = (String) session.getAttribute("personId");
            String newPassword = body.get("password");

            userService.changePassword(personId, newPassword);

            return ResponseEntity.ok(Map.of("message", "パスワードを変更しました"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "変更に失敗しました: " + e.getMessage()));
        }
    }

    // ===========================
    // ユーザー削除
    // ===========================
    @DeleteMapping
    public ResponseEntity<?> deleteUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("personId") == null) {
            return ResponseEntity.status(401)
                .body(Map.of("message", "ログインが必要です"));
        }

        try {
            String personId = (String) session.getAttribute("personId");
            userService.deleteUser(personId);
            session.invalidate();

            return ResponseEntity.ok(Map.of("message", "削除しました"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "削除に失敗しました: " + e.getMessage()));
        }
    }
    // ===========================
    // メールアドレス仮登録
    // ===========================
    @PostMapping("/preregister")
    public ResponseEntity<?> preRegister(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            userService.sendRegistrationEmail(email);
            return ResponseEntity.ok(Map.of("message", "本登録用URLを送信しました"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "送信に失敗しました: " + e.getMessage()));
        }
    }

    // ===========================
    // token検証（本登録前の確認）
    // ===========================
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestParam String token) {
        try {
            userService.verifyToken(token);
            return ResponseEntity.ok(Map.of(
                "message", "tokenは有効です",
                "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body(Map.of("message", e.getMessage()));
        }
    }
}