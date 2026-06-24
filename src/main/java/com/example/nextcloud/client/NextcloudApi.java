package com.example.nextcloud.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestForm;

// Type-safe REST Client interface for Nextcloud API endpoints.
// The base URI is set by QuarkusRestClientBuilder in NextcloudClient
// (programmatic, since the URL is dynamic from AdminConfigStore).
//
// Returns jakarta.ws.rs.core.Response (not typed DTOs) so callers inspect
// status codes and parse raw JSON strings manually. This avoids coupling
// the client to Jackson deserialization and keeps error handling simple.
//
// Headers like Authorization and OCS-APIRequest are passed as @HeaderParam
// method parameters — the caller provides the values at invocation time.
public interface NextcloudApi {

    // POST https://{nextcloud}/apps/oauth2/api/v1/token
    // Exchanges an authorization code for an access token.
    // Body is application/x-www-form-urlencoded (grant_type, client_id, etc.).
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/apps/oauth2/api/v1/token")
    Response exchangeToken(@RestForm MultivaluedMap<String, String> form);

    // GET https://{nextcloud}/ocs/v2.php/cloud/user
    // Returns the authenticated user's ID via the OCS API.
    // Requires OCS-APIRequest: true header (Nextcloud convention).
    @GET
    @Path("/ocs/v2.php/cloud/user")
    Response getUser(@QueryParam("format") String format,
                    @HeaderParam("Authorization") String auth,
                    @HeaderParam("OCS-APIRequest") String ocsApiRequest);

    // MKCOL https://{nextcloud}/remote.php/dav/files/{userId}/Uploads
    // WebDAV MKCOL creates a collection (directory). Ignored (405) if already
    // exists — WebDAV servers return 405 Method Not Allowed for existing dirs.
    @MKCOL
    @Path("/remote.php/dav/files/{userId}/Uploads")
    Response createUploadDir(@HeaderParam("Authorization") String auth,
                            @PathParam("userId") String userId);

    // PUT https://{nextcloud}/remote.php/dav/files/{userId}/Uploads/{filename}
    // WebDAV PUT uploads the raw bytes. Returns 201 Created or 204 No Content.
    @PUT
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/remote.php/dav/files/{userId}/Uploads/{filename}")
    Response uploadFile(@HeaderParam("Authorization") String auth,
                       @PathParam("userId") String userId,
                       @PathParam("filename") String filename,
                       byte[] data);

    // POST https://{nextcloud}/ocs/v2.php/apps/libresign/api/v1/request-signature
    // Creates a signature request on LibreSign. Body is JSON with the base64-encoded
    // PDF, file name, signer info, and status.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ocs/v2.php/apps/libresign/api/v1/request-signature")
    Response requestSignature(@HeaderParam("Authorization") String auth,
                              @HeaderParam("OCS-APIRequest") String ocsApiRequest,
                              @QueryParam("format") String format,
                              String jsonBody);
}
