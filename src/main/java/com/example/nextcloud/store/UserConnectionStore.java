package com.example.nextcloud.store;

import com.example.nextcloud.model.UserToken;
import jakarta.enterprise.context.ApplicationScoped;

// In-memory store for the authenticated user's OAuth token. Same caveats as
// AdminConfigStore: lost on restart/hot-reload, volatile for thread safety.
//
// A future Savapage integration would persist this to a database keyed by
// the local user identity.
@ApplicationScoped
public class UserConnectionStore {
    private volatile UserToken token;

    public UserToken getToken() {
        return token;
    }

    public void setToken(UserToken token) {
        this.token = token;
    }

    public void clear() {
        this.token = null;
    }

    public boolean isConnected() {
        return token != null;
    }
}
