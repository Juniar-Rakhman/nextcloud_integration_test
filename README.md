# Nextcloud OAuth + LibreSign Integration — Proof of Concept

A minimal full-stack proof-of-concept demonstrating Nextcloud OAuth 2.0 authentication and LibreSign digital signature API integration. Built as a single Quarkus application with an embedded Vue 3 frontend.

## Features

- **Admin Configuration** — Configure Nextcloud server URL, OAuth client ID, client secret (app password), and redirect URI
- **OAuth Connect** — Authorization Code flow (no PKCE) to connect a Nextcloud user account
- **LibreSign Signature Request** — Upload a PDF, specify a signer (email + display name), and submit to LibreSign for digital signature
- **WebDAV Upload** (preserved) — Upload files directly to Nextcloud Files via WebDAV
- **Auth Fallback** — When no user OAuth token exists, falls back to the admin's app password for API access

## Stack

**Backend:**
- Java 21, Quarkus 3.14.4, Maven
- `quarkus-rest` + `quarkus-rest-jackson` (RESTEasy Reactive)
- `quarkus-rest-client-jackson` (programmatic REST Client for Nextcloud API)
- In-memory stores (`@ApplicationScoped` volatile fields)

**Frontend:**
- Vue 3, TypeScript, Vite 6, Vue Router 4
- Composition API, no component library
- Built into Quarkus static resources via `frontend-maven-plugin`

## Prerequisites

- Java 21 (JDK)
- A Nextcloud instance with OAuth 2.0 and [LibreSign](https://apps.nextcloud.com/apps/libresign) installed
- A registered OAuth client on Nextcloud (or an app password for direct Bearer auth)

## Quick Start

```bash
# Development mode
./mvnw quarkus:dev

# Full build (installs Node 22, npm ci, builds Vue, copies to Quarkus resources)
./mvnw package
```

Access the app at [http://localhost:8080/app](http://localhost:8080/app).

**Note:** Direct navigation to client-side routes (`/app/admin`, `/app/connect`, `/app/upload`) returns 404. Start at `/app/` and use the navigation bar.

## Usage

### 1. Admin Configuration

Navigate to **Admin** and fill in:

| Field | Example |
|-------|---------|
| Nextcloud URL | `http://cybertron:8080` |
| Client ID | `optimus` |
| Client Secret | App password (e.g. `kfBkc-SxP2c-SZPkF-QwMHd-CtfYA`) |
| Redirect URI | `http://localhost:8080/api/oauth/callback` |

Click **Save**. The connection status shows "Connected as {username} (app password)" when the admin secret is set.

### 2. Request a Signature

Navigate to **Upload**, select a PDF, enter the signer's email and optional display name, then click **Request Signature**.

On success the response shows the LibreSign UUID and filename.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/config` | Get current admin config |
| `POST` | `/api/admin/config` | Save admin config |
| `GET` | `/api/oauth/login` | Get OAuth authorization URL |
| `GET` | `/api/oauth/callback` | OAuth callback (code exchange) |
| `GET` | `/api/oauth/status` | Get connection status |
| `DELETE` | `/api/oauth/disconnect` | Disconnect user |
| `POST` | `/api/files/upload` | Upload file via WebDAV |
| `POST` | `/api/files/request-signature` | Request LibreSign signature |

### LibreSign API (external)

```
POST /ocs/v2.php/apps/libresign/api/v1/request-signature?format=json
Authorization: Bearer {token}
OCS-APIRequest: true
Content-Type: application/json

{
  "file": { "base64": "<base64 PDF>" },
  "name": "document.pdf",
  "status": 0,
  "users": [{ "email": "signer@example.com", "displayName": "Signer Name" }]
}
```

## Architecture

```
nextcloud-integration/
├── pom.xml
├── AGENTS.md
├── src/main/java/com/example/nextcloud/
│   ├── api/          — REST endpoints (AdminConfigResource, OAuthResource, FileResource)
│   ├── service/      — Business logic (NextcloudOAuthService, NextcloudFileService, LibreSignService)
│   ├── client/       — Nextcloud REST Client interface + wrapper (NextcloudApi, NextcloudClient)
│   ├── model/        — DTOs (AdminConfig, UserToken, OAuthStatus, UploadResult, SignRequestResult)
│   └── store/        — In-memory stores (AdminConfigStore, UserConnectionStore)
├── src/main/resources/
│   ├── application.properties
│   └── META-INF/resources/app/  (Vue build output, not committed)
└── ui/src/
    ├── api/          — Fetch wrappers (admin.ts, oauth.ts, files.ts)
    ├── pages/        — AdminPage.vue, ConnectPage.vue, UploadPage.vue
    ├── components/   — Navbar.vue, ConnectionStatus.vue
    ├── router/       — index.ts (createWebHistory)
    └── types/        — index.ts
```

### Key Design Decisions

- **`@Blocking` on resources** — RESTEasy Reactive runs on the Vert.x event loop by default; all OAuth and file resources call blocking HTTP code and must be annotated `@Blocking`
- **Quarkus REST Client** — Programmatic `QuarkusRestClientBuilder` because the Nextcloud base URL is dynamic (from admin config), not static config
- **Auth fallback** — `LibreSignService.resolveAccessToken()` tries the user OAuth token first, then falls back to `AdminConfigStore.clientSecret` (the app password). This avoids requiring a full OAuth login for LibreSign operations
- **`?format=json`** — Required on all OCS API calls; without it Nextcloud returns XML
- **In-memory stores** — `AdminConfigStore` and `UserConnectionStore` use `volatile` fields for cross-thread visibility; lost on Quarkus hot-reload or restart

## Important Caveats

- **Vue SPA routing** — Direct browser navigation to `/app/admin`, `/app/connect`, or `/app/upload` returns 404. Enter at `/app/` and navigate via in-app links
- **No persistent storage** — Config and tokens are in-memory only. Lost on restart or Quarkus dev-mode hot-reload
- **No PKCE** — The OAuth flow uses the simpler Authorization Code grant without PKCE (adequate for this controlled PoC)
- **App password** — The admin `clientSecret` doubles as a Nextcloud app password for Bearer auth. This is a PoC simplification; production should use proper OAuth client credentials
