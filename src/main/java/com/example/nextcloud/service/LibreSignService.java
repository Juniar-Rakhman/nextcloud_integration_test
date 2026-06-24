package com.example.nextcloud.service;

import com.example.nextcloud.client.NextcloudClient;
import com.example.nextcloud.model.AdminConfig;
import com.example.nextcloud.model.SignRequestResult;
import com.example.nextcloud.model.UserToken;
import com.example.nextcloud.store.AdminConfigStore;
import com.example.nextcloud.store.UserConnectionStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Base64;
import java.util.List;
import java.util.Map;

// Handles document signature requests via the LibreSign API.
//
// Uses the user's OAuth token first, falling back to the admin's app
// password (from AdminConfigStore.clientSecret). Both authenticate the
// same Nextcloud user and grant full API access.
//
// Flow:
//   1. Accept a PDF file (bytes), a file name, and signer email(s)
//   2. Base64-encode the PDF
//   3. POST /ocs/v2.php/apps/libresign/api/v1/request-signature with the
//      encoded file and signer list
//   4. Return the result (signature request UUID, node ID, etc.)
@ApplicationScoped
public class LibreSignService {

    @Inject
    UserConnectionStore userConnectionStore;

    @Inject
    AdminConfigStore adminConfigStore;

    @Inject
    NextcloudClient nextcloudClient;

    @Inject
    ObjectMapper objectMapper;

    public SignRequestResult requestSignature(String filename, byte[] fileData, String signerEmail, String signerDisplayName) {
        try {
            String accessToken = resolveAccessToken();
            if (accessToken == null) {
                return new SignRequestResult(false, null, null, null, "Not connected");
            }

            String base64Content = Base64.getEncoder().encodeToString(fileData);

            Map<String, Object> signer = Map.of(
                "email", signerEmail,
                "displayName", signerDisplayName != null ? signerDisplayName : signerEmail
            );

            Map<String, Object> fileObj = Map.of("base64", base64Content);

            Map<String, Object> body = Map.of(
                "file", fileObj,
                "name", filename,
                "status", 0,
                "users", List.of(signer)
            );

            String jsonBody = objectMapper.writeValueAsString(body);
            var response = nextcloudClient.requestSignature(accessToken, jsonBody);

            if (response.statusCode() != 200) {
                String msg = parseErrorMessage(response.body());
                return new SignRequestResult(false, null, null, null, msg);
            }

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode data = json.get("ocs").get("data");
            String uuid = data.get("uuid").asText();
            String nodeId = data.get("nodeId").asText();
            String name = data.get("name").asText();

            return new SignRequestResult(true, uuid, nodeId, name, null);
        } catch (Exception e) {
            return new SignRequestResult(false, null, null, null, e.getMessage());
        }
    }

    // Tries the user's OAuth token first, then falls back to the admin
    // app password (clientSecret). Both authenticate the same Nextcloud
    // user who owns the admin OAuth client registration.
    private String resolveAccessToken() {
        UserToken token = userConnectionStore.getToken();
        if (token != null) {
            return token.getAccessToken();
        }
        AdminConfig config = adminConfigStore.getConfig();
        if (config != null && config.getClientSecret() != null) {
            return config.getClientSecret();
        }
        return null;
    }

    private String parseErrorMessage(String xmlBody) {
        try {
            // LibreSign returns OCS XML on errors, try to extract <message>
            int start = xmlBody.indexOf("<message>");
            int end = xmlBody.indexOf("</message>");
            if (start != -1 && end != -1) {
                return xmlBody.substring(start + 9, end);
            }
        } catch (Exception ignored) {
        }
        return xmlBody;
    }
}
