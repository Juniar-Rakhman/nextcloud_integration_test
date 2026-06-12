package com.example.nextcloud.model;

import java.time.Instant;

// OAuth token + Nextcloud user info for the connected user.
// Stored in UserConnectionStore (in-memory only for this PoC).
//
// Fields:
//   accessToken  — Bearer token for all subsequent API calls
//   refreshToken — present if Nextcloud sent one (currently unused; no
//                  refresh logic is implemented)
//   expiresAt    — token expiry timestamp (currently unused; checked only
//                  if refresh logic is added)
//   userId       — the real Nextcloud user ID from /ocs/v2.php/cloud/user
//   username     — hardcoded "demo-user" per the single-local-user spec
public class UserToken {
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
    private String userId;
    private String username;

    public UserToken() {}

    public UserToken(String accessToken, String refreshToken, Instant expiresAt, String userId, String username) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.userId = userId;
        this.username = username;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
