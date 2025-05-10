package com.capstone.meerkatai.streamingvideo.service;

import com.capstone.meerkatai.cctv.entity.Cctv;
import com.capstone.meerkatai.cctv.repository.CctvRepository;
import com.capstone.meerkatai.streamingvideo.entity.StreamingVideo;
import com.capstone.meerkatai.streamingvideo.repository.StreamingVideoRepository;
import com.capstone.meerkatai.user.entity.User;
import com.capstone.meerkatai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StreamingVideoService {

  private final StreamingVideoRepository streamingVideoRepository;
  private final UserRepository userRepository;
  private final CctvRepository cctvRepository;
  private final RestTemplate restTemplate = new RestTemplate();

  public boolean connectAndRegister(Long userId, Long cctvId, String rtspUrl) {
    //일단 연결 성공 여부는 true로 둠.
    //boolean connectionSuccessful = testRtspConnection(rtspUrl);
    boolean connectionSuccessful = true;

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    Cctv cctv = cctvRepository.findById(cctvId)
            .orElseThrow(() -> new RuntimeException("CCTV not found"));

    StreamingVideo entity = StreamingVideo.builder()
            .user(user)
            .cctv(cctv)
            .streamingVideoStatus(connectionSuccessful)
            .streamingUrl(rtspUrl)
            .startTime(LocalDateTime.now())
            .build();

    streamingVideoRepository.save(entity);

    // FastAPI로 전송
    sendToFastAPI(userId, cctvId, rtspUrl);

    return connectionSuccessful;
  }

  private boolean testRtspConnection(String rtspUrl) {
    try {
      URL url = new URL(rtspUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(2000);
      connection.connect();
      int code = connection.getResponseCode();
      return (code >= 200 && code < 400);
    } catch (Exception e) {
      return false;
    }
  }

  private void sendToFastAPI(Long userId, Long cctvId, String rtspUrl) {
    String fastApiUrl = "http://localhost:8000/api/v1/streaming/start";  // ✅ 올바른 FastAPI URL

    // ✅ FastAPI가 기대하는 형식: snake_case
    Map<String, Object> payload = new HashMap<>();
    payload.put("user_id", userId);
    payload.put("cctv_id", cctvId);
    payload.put("rtsp_url", rtspUrl);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    try {
      restTemplate.postForEntity(fastApiUrl, request, Void.class);
    } catch (Exception e) {
      System.err.println("⚠️ FastAPI 전송 실패: " + e.getMessage());
    }
  }

  public boolean disconnectAndNotify(Long userId, Long cctvId) {
    // 1. 스트리밍 DB 상태 종료 처리 (선택)
    streamingVideoRepository.findByUserUserIdAndCctvCctvId(userId, cctvId)
            .ifPresent(stream -> {
              stream.setStreamingVideoStatus(false);
              stream.setEndTime(LocalDateTime.now());
              streamingVideoRepository.save(stream);
            });

    // 2. FastAPI에 PUT 요청 보내기
    String fastApiUrl = String.format("http://localhost:8000/api/v1/streaming/stop/%d", cctvId);

    try {
      restTemplate.put(fastApiUrl, null);  // body 없이 PUT 요청 가능
      return true;
    } catch (Exception e) {
      System.err.println("❌ FastAPI 스트림 중지 요청 실패: " + e.getMessage());
      return false;
    }
  }

}
