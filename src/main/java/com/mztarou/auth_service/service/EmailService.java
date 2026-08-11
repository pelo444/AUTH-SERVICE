package com.mztarou.auth_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.registration.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationEmail(String toEmail, String token) {
        String registrationUrl = baseUrl + "/verify.html?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("【本登録のご案内】");
        message.setText(
            "以下のURLから本登録を完了してください。\n\n" +
            registrationUrl + "\n\n" +
            "このURLの有効期限は60分です。\n" +
            "心当たりのない場合はこのメールを無視してください。"
        );

        mailSender.send(message);
    }
}