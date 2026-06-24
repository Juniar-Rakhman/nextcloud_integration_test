package com.example.nextcloud.client;

import com.example.nextcloud.store.AdminConfigStore;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Map;

// Thin wrapper around the Quarkus REST Client (NextcloudApi interface).
// Uses QuarkusRestClientBuilder instead of @RegisterRestClient because the
// Nextcloud base URL comes from AdminConfigStore (runtime config), not
// application.properties. Each method builds a fresh client instance from
// the current admin config — safe for this PoC, but in production you would
// cache the client and rebuild it when the config changes.
//
// All methods return ExchangeResponse synchronously. The callers (services)
// are invoked from @Blocking resources, so there is no event-loop benefit
// from async here.
@ApplicationScoped
public class NextcloudClient {

    @Inject
    AdminConfigStore adminConfigStore;

    private NextcloudApi buildApi() {
        String baseUrl = adminConfigStore.getConfig().getNextcloudUrl();
        return QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(baseUrl))
                .build(NextcloudApi.class);
    }

    public ExchangeResponse exchangeToken(Map<String, String> formData) throws Exception {
        MultivaluedHashMap<String, String> form = new MultivaluedHashMap<>();
        formData.forEach(form::add);
        Response resp = buildApi().exchangeToken(form);
        return new ExchangeResponse(resp.getStatus(), resp.readEntity(String.class));
    }

    public ExchangeResponse getUserInfo(String accessToken) throws Exception {
        Response resp = buildApi().getUser(
                "json", "Bearer " + accessToken, "true");
        return new ExchangeResponse(resp.getStatus(), resp.readEntity(String.class));
    }

    public ExchangeResponse createUploadDir(String accessToken, String userId) throws Exception {
        Response resp = buildApi().createUploadDir("Bearer " + accessToken, userId);
        return new ExchangeResponse(resp.getStatus(), resp.readEntity(String.class));
    }

    public ExchangeResponse uploadFile(String accessToken, String userId,
            String filename, byte[] data) throws Exception {
        Response resp = buildApi().uploadFile(
                "Bearer " + accessToken, userId, filename, data);
        return new ExchangeResponse(resp.getStatus(), resp.readEntity(String.class));
    }

    public ExchangeResponse requestSignature(String accessToken, String jsonBody) throws Exception {
        Response resp = buildApi().requestSignature(
                "Bearer " + accessToken, "true", "json", jsonBody);
        return new ExchangeResponse(resp.getStatus(), resp.readEntity(String.class));
    }

    public record ExchangeResponse(int statusCode, String body) {
    }
}
