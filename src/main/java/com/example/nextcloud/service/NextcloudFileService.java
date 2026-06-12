package com.example.nextcloud.service;

import com.example.nextcloud.client.NextcloudClient;
import com.example.nextcloud.model.UploadResult;
import com.example.nextcloud.model.UserToken;
import com.example.nextcloud.store.UserConnectionStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Handles file upload to the connected user's Nextcloud Files via WebDAV.
// Flow:
//   1. MKCOL /remote.php/dav/files/{userId}/Uploads  — create dir (idempotent)
//   2. PUT  /remote.php/dav/files/{userId}/Uploads/{filename} — write file
@ApplicationScoped
public class NextcloudFileService {

    @Inject
    UserConnectionStore userConnectionStore;

    @Inject
    NextcloudClient nextcloudClient;

    public UploadResult uploadFile(String filename, byte[] data) {
        try {
            UserToken token = userConnectionStore.getToken();
            if (token == null) {
                return new UploadResult(false, null, "Not connected");
            }

            String userId = token.getUserId();
            String accessToken = token.getAccessToken();

            // Create target directory. WebDAV servers return 405 if the
            // directory already exists; we ignore it.
            nextcloudClient.createUploadDir(accessToken, userId);

            var response = nextcloudClient.uploadFile(accessToken, userId, filename, data);

            int status = response.statusCode();
            if (status == 201 || status == 204) {
                return new UploadResult(true, "Uploads/" + filename);
            } else {
                return new UploadResult(false, null, "Upload failed with status " + status);
            }
        } catch (Exception e) {
            return new UploadResult(false, null, e.getMessage());
        }
    }
}
