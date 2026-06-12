package com.example.nextcloud.client;

import jakarta.ws.rs.HttpMethod;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// JAX-RS does not define a built-in annotation for the WebDAV MKCOL method.
// This annotation, meta-annotated with @HttpMethod("MKCOL"), makes it
// available for use on REST Client interface methods. The Quarkus REST
// Client (RESTEasy Reactive) interprets it just like @GET or @PUT.
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("MKCOL")
public @interface MKCOL {
}
