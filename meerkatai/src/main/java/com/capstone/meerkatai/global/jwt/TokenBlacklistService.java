package com.capstone.meerkatai.global.jwt;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TokenBlacklistService {
  private final ConcurrentMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

  public void blacklistToken(String token, Long expirationTime) {
    blacklistedTokens.put(token, expirationTime);
  }

  public boolean isBlacklisted(String token) {
    if (!blacklistedTokens.containsKey(token)) {
      return false;
    }

    // 만료된 토큰은 블랙리스트에서 제거
    if (blacklistedTokens.get(token) < System.currentTimeMillis()) {
      blacklistedTokens.remove(token);
      return false;
    }

    return true;
  }
}