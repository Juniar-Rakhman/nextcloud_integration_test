package com.example.nextcloud.service;

import com.example.nextcloud.client.NextcloudClient;
import com.example.nextcloud.model.AdminConfig;
import com.example.nextcloud.model.OAuthStatus;
import com.example.nextcloud.model.UserToken;
import com.example.nextcloud.store.AdminConfigStore;
import com.example.nextcloud.store.UserConnectionStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

// Orchestrates the Nextcloud OAuth 2.0 Authorization Code flow (no PKCE).
// All methods are blocking (call .toCompletableFuture().get() internally via
// NextcloudClient); callers must ensure @Blocking on the JAX-RS resource.
@ApplicationScoped
public class NextcloudOAuthService {

    @Inject
    AdminConfigStore adminConfigStore;

    @Inject
    UserConnectionStore userConnectionStore;

    @Inject
    NextcloudClient nextcloudClient;

    @Inject
    ObjectMapper objectMapper;

    // Build the Nextcloud OAuth authorize URL. The user's browser is
    // redirected here to grant access. State is generated but not validated
    // on callback (PoC simplification — production should verify it to
    // prevent CSRF).
    public String buildAuthorizationUrl() {
        AdminConfig config = adminConfigStore.getConfig();
        if (config == null) {
            throw new IllegalStateException("Admin not configured");
        }
        String state = UUID.randomUUID().toString();
        return config.getNextcloudUrl() + "/apps/oauth2/authorize"
                + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + config.getRedirectUri()
                + "&response_type=code"
                + "&state=" + state;
    }

    // Exchange the authorization code for an access token via Nextcloud's
    // OAuth token endpoint, then fetch the real user ID via OCS API.
    // Stores the combined UserToken (access token + user info) in memory.
    public UserToken exchangeCode(String code) throws Exception {
        AdminConfig config = adminConfigStore.getConfig();
        var formData = Map.of(
            "grant_type", "authorization_code",
            "client_id", config.getClientId(),
            "client_secret", config.getClientSecret(),
            "redirect_uri", config.getRedirectUri(),
            "code", code
        );

        var response = nextcloudClient.exchangeToken(formData);
        if (response.statusCode() != 200) {
            throw new RuntimeException("Token exchange failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        String accessToken = json.get("access_token").asText();
        String refreshToken = json.has("refresh_token") ? json.get("refresh_token").asText() : null;
        int expiresIn = json.has("expires_in") ? json.get("expires_in").asInt() : 3600;
        Instant expiresAt = Instant.now().plusSeconds(expiresIn);

        // Fetch the actual Nextcloud user ID (e.g. "optimus") rather than
        // relying on the hardcoded "demo-user" local user name.
        String userId = fetchUserId(accessToken);

        // username is hardcoded to "demo-user" per the single-local-user
        // assumption in the spec. In Savapage this would be the actual
        // authenticated user.
        UserToken token = new UserToken(accessToken, refreshToken, expiresAt, userId, "demo-user");
        userConnectionStore.setToken(token);
        return token;
    }

    // GET /ocs/v2.php/cloud/user with the Bearer token extracts the
    // Nextcloud user ID (ocs.data.id) from the OCS JSON response.
    private String fetchUserId(String accessToken) throws Exception {
        var response = nextcloudClient.getUserInfo(accessToken);
        if (response.statusCode() != 200) {
            throw new RuntimeException("User info fetch failed: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.get("ocs").get("data").get("id").asText();
    }

    public OAuthStatus getStatus() {
        UserToken token = userConnectionStore.getToken();
        if (token != null) {
            return new OAuthStatus(true, token.getUsername());
        }
        AdminConfig config = adminConfigStore.getConfig();
        if (config != null && config.getClientSecret() != null) {
            return new OAuthStatus(true, "optimus (app password)");
        }
        return new OAuthStatus(false, null);
    }

    public void disconnect() {
        userConnectionStore.clear();
    }
}
