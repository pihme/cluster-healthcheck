package com.github.pihme.clusterhealthcheck.k8s;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import io.kubernetes.client.util.authenticators.Authenticator;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class ReplacedGCPAuthenticator implements Authenticator {

  private static final String ACCESS_TOKEN = "access-token";
  private static final String EXPIRY = "expiry";

  private final GoogleCredentials credentials;

  public ReplacedGCPAuthenticator(final GoogleCredentials credentials) {
    this.credentials = credentials;
  }

  @Override
  public String getName() {
    return "gcp";
  }

  @Override
  public String getToken(final Map<String, Object> config) {
    return (String) config.get("access-token");
  }

  @Override
  public boolean isExpired(final Map<String, Object> config) {
    final Object expiryObj = config.get("expiry");
    final Instant expiry;
    if (expiryObj instanceof Date) {
      expiry = ((Date) expiryObj).toInstant();
    } else if (expiryObj instanceof Instant) {
      expiry = (Instant) expiryObj;
    } else {
      if (!(expiryObj instanceof String)) {
        throw new RuntimeException("Unexpected object type: " + expiryObj.getClass());
      }

      expiry = Instant.parse((String) expiryObj);
    }

    return expiry != null && expiry.compareTo(Instant.now()) <= 0;
  }

  @Override
  public Map<String, Object> refresh(final Map<String, Object> config) {
    try {
      final AccessToken accessToken = credentials.refreshAccessToken();

      config.put(ACCESS_TOKEN, accessToken.getTokenValue());
      config.put(EXPIRY, accessToken.getExpirationTime());
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return config;
  }
}