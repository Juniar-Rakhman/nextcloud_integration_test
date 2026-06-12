# AGENTS.md — Nextcloud OAuth Integration PoC

## Commands

| Command | Purpose |
|---------|---------|
| `./mvnw quarkus:dev` | Dev mode on port 8080 |
| `./mvnw package` | Full build (installs Node 22, npm ci, vue-tsc + vite build, copies to Quarkus resources) |

No tests exist (`prompt.md` explicitly excludes them).

## Architecture

- **Quarkus 3.14.4** / Java 21 / RESTEasy Reactive (`quarkus-rest` + `quarkus-rest-jackson` — NOT the deprecated `quarkus-resteasy-reactive`)
- **Vue 3 + Vite 6** frontend built into `src/main/resources/META-INF/resources/app/`, served at `/app`
- **In-memory stores** (`AdminConfigStore`, `UserConnectionStore` via `@ApplicationScoped`). Lost on Quarkus dev-mode hot-reload.
- Three pages: Admin (`/app/admin`), Connect (`/app/connect`), Upload (`/app/upload`)
- **No database, no Docker, no tests**

## HTTP Client — Important

`NextcloudClient` uses `java.net.http.HttpClient` (Java 21 built-in), **NOT** Vert.x WebClient. The Vert.x WebClient was replaced because its event-loop threading deadlocked with RESTEasy Reactive's event-loop thread when `.toCompletableFuture().get()` was called. Despite this, `WebClientProducer.java` and the `quarkus-vertx` + `vertx-web-client` Maven deps are still present in the codebase (legacy).

## REST Endpoints — `@Blocking` Required

All JAX-RS resource classes that call services using `.toCompletableFuture().get()` must be annotated `@Blocking` (`io.smallrye.common.annotation.Blocking`). Currently applied to `OAuthResource`. RESTEasy Reactive runs on the Vert.x event loop by default; blocking kills it.

## Directories

```
src/main/java/com/example/nextcloud/
  api/        — JAX-RS resources (AdminConfigResource, OAuthResource, FileResource)
  service/    — Business logic (NextcloudOAuthService, NextcloudFileService)
  client/     — NextcloudClient (HTTP calls), WebClientProducer (orphaned)
  model/      — DTOs (AdminConfig, UserToken, OAuthStatus, UploadResult)
  store/      — In-memory state (AdminConfigStore, UserConnectionStore)
ui/src/
  api/        — Fetch wrappers (admin.ts, oauth.ts, files.ts)
  pages/      — AdminPage.vue, ConnectPage.vue, UploadPage.vue
  components/ — Navbar.vue, ConnectionStatus.vue
  router/     — index.ts (createWebHistory, not hash mode)
  types/      — index.ts
```

## OAuth Flow

1. Admin configures Nextcloud URL, client ID, secret, redirect URI via `/api/admin/config`
2. User clicks Connect → frontend `GET /api/oauth/login` → returns `{redirectUrl}` → browser redirects to Nextcloud OAuth authorize page (no PKCE)
3. User grants access → Nextcloud redirects to `/api/oauth/callback?code=...` → backend exchanges code for token via `POST /apps/oauth2/api/v1/token` → stores token in memory → redirects to `/app/connect`
4. OAuth redirect URI on Nextcloud must exactly match the configured value (`http://localhost:8080/api/oauth/callback`)

## Upload Flow

- Creates `Uploads/` directory via WebDAV MKCOL (ignores 405 if already exists)
- Uploads file via `PUT /remote.php/dav/files/{userId}/Uploads/{filename}`

## Vue SPA Quirk

Router uses `createWebHistory()` (no hash). Direct browser navigation to `/app/connect`, `/app/upload`, or `/app/admin` returns 404 because Quarkus doesn't know these paths. Must enter at `/app/` and navigate via in-app links.

## Existing OAuth Client

The registered app password is `kfBkc-SxP2c-SZPkF-QwMHd-CtfYA` for user `optimus` on Nextcloud `cybertron:8080`. Do not commit.

## User ID

`NextcloudOAuthService.exchangeCode()` calls `GET /ocs/v2.php/cloud/user` to fetch the real user ID from Nextcloud (not hardcoded). The `UserToken` stores both `userId` and `username` ("demo-user").
