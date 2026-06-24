package com.example.nextcloud.api;

import com.example.nextcloud.model.SignRequestResult;
import com.example.nextcloud.model.UploadResult;
import com.example.nextcloud.service.LibreSignService;
import com.example.nextcloud.service.NextcloudFileService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import java.nio.file.Files;

// REST endpoints for file operations:
//
//   POST /api/files/upload            — multipart form, upload via WebDAV
//   POST /api/files/request-signature — multipart form, create LibreSign signature request
@Path("/api/files")
@Produces(MediaType.APPLICATION_JSON)
public class FileResource {

    @Inject
    NextcloudFileService fileService;

    @Inject
    LibreSignService libreSignService;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@RestForm("file") FileUpload file) {
        if (file == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new UploadResult(false, null, "No file provided"))
                    .build();
        }
        try {
            byte[] data = Files.readAllBytes(file.filePath());
            String filename = file.fileName();
            UploadResult result = fileService.uploadFile(filename, data);
            if (result.isSuccess()) {
                return Response.ok(result).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new UploadResult(false, null, e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/request-signature")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Blocking
    public Response requestSignature(
            @RestForm("file") FileUpload file,
            @RestForm("signerEmail") String signerEmail,
            @RestForm("signerDisplayName") String signerDisplayName) {
        if (file == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new SignRequestResult(false, null, null, null, "No file provided"))
                    .build();
        }
        if (signerEmail == null || signerEmail.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new SignRequestResult(false, null, null, null, "Signer email is required"))
                    .build();
        }
        try {
            byte[] data = Files.readAllBytes(file.filePath());
            String filename = file.fileName();
            SignRequestResult result = libreSignService.requestSignature(filename, data, signerEmail, signerDisplayName);
            if (result.isSuccess()) {
                return Response.ok(result).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new SignRequestResult(false, null, null, null, e.getMessage()))
                    .build();
        }
    }
}
