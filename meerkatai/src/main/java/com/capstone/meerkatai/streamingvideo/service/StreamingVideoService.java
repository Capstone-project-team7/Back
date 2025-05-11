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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreamingVideoService {

  private final StreamingVideoRepository streamingVideoRepository;
  private final UserRepository userRepository;
  private final CctvRepository cctvRepository;
  private final RestTemplate restTemplate = new RestTemplate();

  public boolean connectAndRegister(Long userId, Long targetCctvId, String rtspUrl) {
    //일단 연결 성공 여부는 true로 둠.
    //boolean connectionSuccessful = testRtspConnection(rtspUrl);
    boolean connectionSuccessful = true;

    try {
      // 1. 해당 사용자의 모든 활성 스트리밍 조회
      List<StreamingVideo> activeStreams = streamingVideoRepository.findByUserUserIdAndStreamingVideoStatusTrue(userId);

      // 2. 다른 CCTV에 대한 스트리밍을 모두 비활성화 및 FastAPI에 중지 요청
      for (StreamingVideo stream : activeStreams) {
        Long cctvId = stream.getCctv().getCctvId();
        if (!cctvId.equals(targetCctvId)) {
          stream.setStreamingVideoStatus(false);
          stream.setEndTime(LocalDateTime.now());
          streamingVideoRepository.save(stream);

          // FastAPI에 중지 요청
          String fastApiUrl = String.format("http://localhost:8000/api/v1/streaming/stop/%d", cctvId);
          try {
            restTemplate.put(fastApiUrl, null);
          } catch (Exception e) {
            System.err.println("⚠️ FastAPI 중지 요청 실패 (cctvId=" + cctvId + "): " + e.getMessage());
          }
        }
      }

      // 3. 연결 시도할 CCTV 스트리밍Video 상태 확인 또는 생성 후 업데이트
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found"));
      Cctv cctv = cctvRepository.findById(targetCctvId)
              .orElseThrow(() -> new RuntimeException("CCTV not found"));

      Optional<StreamingVideo> existingStreamOpt = streamingVideoRepository.findByUserUserIdAndCctvCctvId(userId, targetCctvId);

      StreamingVideo entity;
      if (existingStreamOpt.isPresent()) {
        // 이미 존재하는 경우 상태 갱신
        entity = existingStreamOpt.get();
        entity.setStreamingVideoStatus(connectionSuccessful);
        entity.setStreamingUrl(rtspUrl);
        entity.setStartTime(LocalDateTime.now());
      } else {
        // 없으면 새로 생성
        entity = StreamingVideo.builder()
                .user(user)
                .cctv(cctv)
                .streamingVideoStatus(connectionSuccessful)
                .streamingUrl(rtspUrl)
                .startTime(LocalDateTime.now())
                .build();
      }

      streamingVideoRepository.save(entity);

      // FastAPI로 전송
      sendToFastAPI(userId, targetCctvId, rtspUrl);

      return connectionSuccessful;
    } catch (Exception e) {
      System.err.println("❌ 연결 처리 중 예외 발생: " + e.getMessage());
      return false;
    }
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
    String fastApiUrl = "http://localhost:8000/api/v1/streaming/stop";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> body = new HashMap<>();
    body.put("user_id", userId);
    body.put("cctv_id", cctvId);

    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    try {
      // ✅ FastAPI에 스트리밍 중지 요청 먼저
      restTemplate.exchange(fastApiUrl, HttpMethod.PUT, requestEntity, Void.class);

      // ✅ 성공한 경우에만 DB 상태 변경
      streamingVideoRepository.findByUserUserIdAndCctvCctvId(userId, cctvId)
              .ifPresent(stream -> {
                stream.setStreamingVideoStatus(false);
                stream.setEndTime(LocalDateTime.now());
                streamingVideoRepository.save(stream);
              });

      return true;

    } catch (Exception e) {
      System.err.println("❌ FastAPI 스트림 중지 요청 실패: " + e.getMessage());
      return false;
    }
  }
}
