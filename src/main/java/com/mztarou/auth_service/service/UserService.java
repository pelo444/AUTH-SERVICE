package com.mztarou.auth_service.service;

import com.mztarou.auth_service.entity.User;
import com.mztarou.auth_service.entity.UserAuth;
import com.mztarou.auth_service.repository.UserAuthRepository;
import com.mztarou.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mztarou.auth_service.entity.RegistrationToken;
import com.mztarou.auth_service.repository.RegistrationTokenRepository;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationTokenRepository registrationTokenRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                    UserAuthRepository userAuthRepository,
                    PasswordEncoder passwordEncoder,
                    RegistrationTokenRepository registrationTokenRepository,
                    EmailService emailService) {
        this.userRepository = userRepository;
        this.userAuthRepository = userAuthRepository;
        this.passwordEncoder = passwordEncoder;
        this.registrationTokenRepository = registrationTokenRepository;
        this.emailService = emailService;
    }

    // ===========================
    // 新規ユーザー登録
    // ===========================
    @Transactional
    public User registerUser(String username, String personId, String rawPassword) {

        // 同一PERSON_IDの有効なレコードが存在する場合はエラー
        userRepository.findByPersonIdAndInvalidTimeIsNull(personId)
            .ifPresent(existing -> {
                throw new RuntimeException("このPERSON_IDはすでに登録されています: " + personId);
            });

        // USERSに新規レコードを挿入（IDはシーケンスで自動採番）
        User user = new User(username, personId, LocalDateTime.now());
        user = userRepository.save(user);

        // USER_AUTHSにも同じIDで挿入
        String hashedPassword = passwordEncoder.encode(rawPassword);
        UserAuth userAuth = new UserAuth(
            user.getId(),
            user.getPersonId(),
            "email",
            hashedPassword,
            LocalDateTime.now()
        );
        userAuthRepository.save(userAuth);

        return user;
    }

    // ===========================
    // username変更
    // ===========================
    @Transactional
    public User changeUsername(String personId, String newUsername) {

        // 現在の有効レコードを取得
        User currentUser = userRepository
            .findByPersonIdAndInvalidTimeIsNull(personId)
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + personId));

        // 現在の認証情報からパスワードを取得して古いレコードを削除
        String currentIdentifier = userAuthRepository
            .findByPersonId(currentUser.getPersonId())
            .map(auth -> {
                String identifier = auth.getIdentifier();
                userAuthRepository.delete(auth);
                userAuthRepository.flush();
                return identifier;
            })
            .orElseThrow(() -> new RuntimeException("認証情報が見つかりません"));

        // 現在のUSERSを無効化
        currentUser.setInvalidTime(LocalDateTime.now());
        userRepository.saveAndFlush(currentUser);

        // 新しいusernameで新規レコードを挿入
        User newUser = new User(newUsername, personId, LocalDateTime.now());
        newUser = userRepository.save(newUser);

        // USER_AUTHSに新規レコードを挿入（パスワードは引き継ぐ）
        UserAuth newAuth = new UserAuth(
            newUser.getId(),
            newUser.getPersonId(),
            "email",
            currentIdentifier,
            LocalDateTime.now()
        );
        userAuthRepository.save(newAuth);

        return newUser;
    }

    // ===========================
    // パスワード変更
    // ===========================
    @Transactional
    public void changePassword(String personId, String newRawPassword) {

        // 有効なUSERSレコードを取得
        User user = userRepository
            .findByPersonIdAndInvalidTimeIsNull(personId)
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + personId));

        // USER_AUTHSのIDENTIFIERを上書き
        UserAuth userAuth = userAuthRepository
            .findByPersonId(user.getPersonId())
            .orElseThrow(() -> new RuntimeException("認証情報が見つかりません"));

        userAuth.setIdentifier(passwordEncoder.encode(newRawPassword));
        userAuthRepository.save(userAuth);
    }

    // ===========================
    // ユーザー削除
    // ===========================
    @Transactional
    public void deleteUser(String personId) {

        // USERSを無効化
        User user = userRepository
            .findByPersonIdAndInvalidTimeIsNull(personId)
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + personId));

        user.setInvalidTime(LocalDateTime.now());
        userRepository.save(user);

        // USER_AUTHSを削除
        userAuthRepository.findByPersonId(user.getPersonId())
            .ifPresent(userAuthRepository::delete);
    }

    // ===========================
    // 有効ユーザーの取得
    // ===========================
    public Optional<User> findActiveUser(String personId) {
        return userRepository.findByPersonIdAndInvalidTimeIsNull(personId);
    }
    // ===========================
    // メールアドレス仮登録（token生成・メール送信）
    // ===========================
    @Transactional
    public void sendRegistrationEmail(String email) {

        // 既存の未使用tokenがあれば無効化
        registrationTokenRepository
            .findByEmailAndUsedAtIsNull(email)
            .ifPresent(existing -> {
                existing.setUsedAt(LocalDateTime.now());
                registrationTokenRepository.save(existing);
            });

        // 新しいtokenを生成
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();
        RegistrationToken registrationToken = new RegistrationToken(
            token,
            email,
            now,
            now.plusMinutes(60)
        );
        registrationTokenRepository.save(registrationToken);

        // メール送信
        emailService.sendRegistrationEmail(email, token);
    }

    // ===========================
    // token検証
    // ===========================
    public RegistrationToken verifyToken(String token) {
        RegistrationToken registrationToken = registrationTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new RuntimeException("無効なtokenです"));

        if (!registrationToken.isValid()) {
            throw new RuntimeException("tokenが期限切れまたは使用済みです");
        }

        return registrationToken;
    }
    // ===========================
    // tokenを使用済みにする
    // ===========================
    @Transactional
    public void markTokenAsUsed(String token) {
        registrationTokenRepository
            .findByToken(token)
            .ifPresent(t -> {
                t.setUsedAt(LocalDateTime.now());
                registrationTokenRepository.save(t);
            });
    }
}