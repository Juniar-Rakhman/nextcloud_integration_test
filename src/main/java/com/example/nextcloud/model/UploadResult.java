package com.example.nextcloud.model;

// DTO returned by POST /api/files/upload.
//
// On success:  { success: true,  path: "Uploads/filename" }
// On failure:  { success: false, error: "reason" }
public class UploadResult {
    private boolean success;
    private String path;
    private String error;

    public UploadResult() {}

    public UploadResult(boolean success, String path) {
        this.success = success;
        this.path = path;
    }

    public UploadResult(boolean success, String path, String error) {
        this.success = success;
        this.path = path;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
