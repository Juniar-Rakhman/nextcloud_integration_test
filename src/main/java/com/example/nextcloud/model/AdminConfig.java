package com.example.nextcloud.model;

// Admin-provided Nextcloud connection settings.
// Stored in AdminConfigStore (in-memory only for this PoC).
public class AdminConfig {
    private String nextcloudUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUri;

    public AdminConfig() {}

    public AdminConfig(String nextcloudUrl, String clientId, String clientSecret, String redirectUri) {
        this.nextcloudUrl = nextcloudUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String getNextcloudUrl() { return nextcloudUrl; }
    public void setNextcloudUrl(String nextcloudUrl) { this.nextcloudUrl = nextcloudUrl; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}
