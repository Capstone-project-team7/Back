package com.capstone.meerkatai.alarm.service;

import com.capstone.meerkatai.alarm.dto.AnomalyVideoMetadataRequest;
import com.capstone.meerkatai.global.service.S3Service;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Value("${APP_EMAIL}")
    private String appEmail;

    //메타데이터에서 사용자 이메일 조회 및 이메일 발송 로직
    public String processAndSendAnomalyEmail(AnomalyVideoMetadataRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElse(null);

        if (user == null) {
            return "User not found";
        }

        if (!user.isNotification()) {
            return "Notification is disabled for this user";
        }

        String videoUrl = request.getVideoUrl();
        
        // S3 URL인 경우 presigned URL 생성하여 포함
        try {
            if (s3Service.isS3Url(videoUrl)) {
                String objectKey = s3Service.extractS3Key(videoUrl);
                URL presignedUrl = s3Service.generatePresignedUrlForDownload(objectKey);
                videoUrl = presignedUrl.toString();
                log.info("S3 비디오 URL에 대한 presigned URL 생성: {}", videoUrl);
            }
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패: {}", e.getMessage());
        }

        String subject = "[AI 이상행동 감지 알림]";
        String body = String.format(
                "이상행동이 감지되었습니다.\n\n▶ 유형: %s\n▶ 발생 시간: %s\n▶ 영상 확인: %s\n",
                request.getAnomalyType(),
                request.getTimestamp(),
                videoUrl
        );

        return sendEmail(user.getEmail(), subject, body);
    }

    private String sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(appEmail);

        try {
            mailSender.send(message);
            log.info("✅ 이메일 전송 성공: {}", to);
            return "Email sent successfully";
        } catch (Exception e) {
            log.error("❌ 이메일 전송 실패: {}", to, e);
            return "Failed to send email";
        }
    }
}
