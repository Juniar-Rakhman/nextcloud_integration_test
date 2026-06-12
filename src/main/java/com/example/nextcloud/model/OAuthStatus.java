package com.example.nextcloud.model;

// Lightweight DTO returned by GET /api/oauth/status.
// Consumed by the Vue frontend to show connection state in the navbar.
public class OAuthStatus {
    private boolean connected;
    private String username;

    public OAuthStatus() {}

    public OAuthStatus(boolean connected, String username) {
        this.connected = connected;
        this.username = username;
    }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
