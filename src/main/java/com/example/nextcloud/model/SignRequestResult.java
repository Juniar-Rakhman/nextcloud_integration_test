package com.example.nextcloud.model;

// DTO returned by POST /api/files/request-signature.
//
// On success: { success: true,  uuid: "957d1f11-...", nodeId: "215", name: "file.pdf" }
// On failure: { success: false, error: "reason" }
public class SignRequestResult {
    private boolean success;
    private String uuid;
    private String nodeId;
    private String name;
    private String error;

    public SignRequestResult() {}

    public SignRequestResult(boolean success, String uuid, String nodeId, String name, String error) {
        this.success = success;
        this.uuid = uuid;
        this.nodeId = nodeId;
        this.name = name;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
