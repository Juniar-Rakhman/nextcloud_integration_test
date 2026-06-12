package com.example.nextcloud.api;

import com.example.nextcloud.model.AdminConfig;
import com.example.nextcloud.store.AdminConfigStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// REST endpoints for the admin Nextcloud configuration.
// Config is stored in-memory only — lost on restart (see AdminConfigStore).
//
//   GET  /api/admin/config  — returns current config (or empty placeholders)
//   POST /api/admin/config  — saves config
//
// @Blocking is NOT needed here because the store is a simple field read/write
// with no blocking I/O.
@Path("/api/admin/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminConfigResource {

    @Inject
    AdminConfigStore store;

    @GET
    public Response getConfig() {
        AdminConfig config = store.getConfig();
        if (config == null) {
            return Response.ok(new AdminConfig("", "", "", "")).build();
        }
        return Response.ok(config).build();
    }

    @POST
    public Response saveConfig(AdminConfig config) {
        store.setConfig(config);
        return Response.ok(config).build();
    }
}
