package com.example.nextcloud.api;

import com.example.nextcloud.model.OAuthStatus;
import com.example.nextcloud.service.NextcloudOAuthService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Map;

// REST endpoints for the OAuth 2.0 Authorization Code flow.
//
// @Blocking is required because NextcloudOAuthService calls
// NextcloudClient methods that block on HTTP responses. Without @Blocking,
// RESTEasy Reactive would run this on the Vert.x event loop, and blocking
// it would stall the entire event loop (deadlock).
//
// Endpoints:
//   GET  /api/oauth/login      — returns {redirectUrl} for Nextcloud authorize
//   GET  /api/oauth/callback   — OAuth redirect target, exchanges code for token
//   GET  /api/oauth/status     — returns {connected, username}
//   DELETE /api/oauth/disconnect — clears stored token
@Path("/api/oauth")
@Produces(MediaType.APPLICATION_JSON)
@Blocking
public class OAuthResource {

    @Inject
    NextcloudOAuthService oauthService;

    @GET
    @Path("/login")
    public Response login() {
        try {
            String redirectUrl = oauthService.buildAuthorizationUrl();
            return Response.ok(Map.of("redirectUrl", redirectUrl)).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    // Nextcloud redirects here after the user grants access.
    // The `state` parameter is accepted but not validated (PoC simplification).
    @GET
    @Path("/callback")
    public Response callback(@QueryParam("code") String code, @QueryParam("state") String state) {
        if (code == null || code.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Missing authorization code")).build();
        }
        try {
            oauthService.exchangeCode(code);
            return Response.seeOther(URI.create("/app/connect")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "OAuth exchange failed: " + e.getMessage())).build();
        }
    }

    @GET
    @Path("/status")
    public Response status() {
        OAuthStatus status = oauthService.getStatus();
        return Response.ok(status).build();
    }

    @DELETE
    @Path("/disconnect")
    public Response disconnect() {
        oauthService.disconnect();
        return Response.ok(Map.of("success", true)).build();
    }
}
