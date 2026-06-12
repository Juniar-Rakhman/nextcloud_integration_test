package com.example.nextcloud.store;

import com.example.nextcloud.model.AdminConfig;
import jakarta.enterprise.context.ApplicationScoped;

// In-memory store for the admin Nextcloud configuration (URL, OAuth client
// credentials, redirect URI). Data lives in the JVM heap only — lost on
// restart or Quarkus dev-mode hot-reload. This is intentional per the PoC
// spec; Savapage integration will replace this with a persistent store.
//
// volatile: guarantees that a write from one thread (e.g. a REST call on a
// worker thread) is immediately visible to any subsequent read on another
// thread. Without it, the JIT could cache the field per-thread, and on weakly-
// ordered architectures (ARM) the store might not be visible across cores at
// all. This is cheap (single read/write barrier) and sufficient here because
// AdminConfig is written once and read many times — no compound operations
// need atomicity.
@ApplicationScoped
public class AdminConfigStore {
    private volatile AdminConfig config;

    public AdminConfig getConfig() {
        return config;
    }

    public void setConfig(AdminConfig config) {
        this.config = config;
    }
}
