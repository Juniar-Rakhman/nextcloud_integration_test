package com.example.nextcloud.api;

import com.example.nextcloud.model.UploadResult;
import com.example.nextcloud.service.NextcloudFileService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import java.nio.file.Files;

// REST endpoint for file upload to Nextcloud Files.
//
//   POST /api/files/upload  — multipart form with "file" part
//
// Reads the uploaded file bytes and delegates to NextcloudFileService, which
// creates the Uploads/ directory (MKCOL) and PUTs the file via WebDAV.
@Path("/api/files")
@Produces(MediaType.APPLICATION_JSON)
public class FileResource {

    @Inject
    NextcloudFileService fileService;

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
}
