# Build a Minimal Quarkus + Embedded Vue Demo: Nextcloud OAuth Integration

Create a minimal full-stack proof-of-concept demonstrating:

- Admin configures Nextcloud server
- User connects their own Nextcloud account
- User uploads files into Nextcloud Files

The goal is to validate architecture for later implementation inside Savapage.

Use a **single Quarkus application**.

Frontend must be served by Quarkus.

No separate frontend server.

---

# Stack

Backend:

- Java 21
- Quarkus 3
- Maven
- RESTEasy Reactive
- Jackson
- Vert.x Web Client
- SmallRye Config
- Qute only if useful
- No database
- Store data in memory

Frontend:

- Vue 3
- Vite
- TypeScript
- Vue Router
- Composition API
- Build static assets into Quarkus

Build:

- Maven only

---

# Architecture

```text
project
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.example.nextcloud
│   │   └── resources
│   │       ├── META-INF
│   │       │   └── resources
│   │       │       └── app
│   │       └── application.properties
│
└── ui
    ├── src
    ├── package.json
    └── vite.config.ts
```

Vue builds into:

```text
src/main/resources/META-INF/resources/app
```

Quarkus serves:

```text
http://localhost:8080/app
```

API:

```text
http://localhost:8080/api/*
```

---

# Requirements

Application contains 3 pages.

---

# Page 1 — Admin

Route:

```text
/app/admin
```

Purpose:

Configure global Nextcloud integration.

Fields:

```text
Nextcloud URL
Client ID
Client Secret
Redirect URI
```

Buttons:

```text
Save
Reset
```

Example:

```text
http://localhost:8088
client123
secret123
http://localhost:8080/api/oauth/callback
```

Backend:

```http
GET /api/admin/config
POST /api/admin/config
```

Request:

```json
{
  "nextcloudUrl": "",
  "clientId": "",
  "clientSecret": "",
  "redirectUri": ""
}
```

Store in memory.

Use:

```java
@ApplicationScoped
class AdminConfigStore
```

---

# Page 2 — Connect

Route:

```text
/app/connect
```

Purpose:

Connect current user to Nextcloud.

Assume one local user:

```text
demo-user
```

UI:

If disconnected:

```text
[ Connect to Nextcloud ]
```

If connected:

```text
Connected
[ Disconnect ]
```

Flow:

Frontend:

```http
GET /api/oauth/login
```

Response:

```json
{
  "redirectUrl": "..."
}
```

Browser redirects.

OAuth URL:

```text
{nextcloud}/apps/oauth2/authorize
```

Parameters:

```text
client_id
redirect_uri
response_type=code
state
```

No PKCE.

Callback:

```http
GET /api/oauth/callback
```

Exchange:

```text
POST
/apps/oauth2/api/v1/token
```

Request:

```text
grant_type=authorization_code
client_id
client_secret
redirect_uri
code
```

Save:

```java
class UserToken {
    String accessToken;
    String refreshToken;
    Instant expiresAt;
}
```

Store:

```java
@ApplicationScoped
class UserConnectionStore
```

Provide:

```http
GET /api/oauth/status
DELETE /api/oauth/disconnect
```

---

# Page 3 — Upload

Route:

```text
/app/upload
```

Purpose:

Upload file into authenticated user's Nextcloud Files.

If disconnected:

```text
Please connect first.
```

If connected:

```text
Choose File
Upload
```

Backend:

```http
POST /api/files/upload
```

Multipart upload.

Before upload:

Call:

```http
GET /ocs/v2.php/cloud/user
```

Headers:

```text
Authorization: Bearer TOKEN
OCS-APIRequest: true
```

Extract:

```text
ocs.data.id
```

Upload:

```http
PUT
/remote.php/dav/files/{id}/Uploads/{filename}
```

Headers:

```text
Authorization: Bearer TOKEN
```

Return:

```json
{
  "success": true,
  "path": "Uploads/test.pdf"
}
```

---

# Backend Structure

```text
api
service
client
model
store
```

Classes:

```text
AdminConfigResource
OAuthResource
FileResource

NextcloudClient
NextcloudOAuthService
NextcloudFileService

AdminConfigStore
UserConnectionStore
```

Use:

```text
Vert.x WebClient
```

Do not use blocking HTTP clients.

---

# Frontend Structure

```text
ui/src
 ├── api
 ├── pages
 ├── router
 ├── components
 └── types
```

Pages:

```text
AdminPage.vue
ConnectPage.vue
UploadPage.vue
```

Components:

```text
Navbar.vue
ConnectionStatus.vue
```

---

# UI Requirements

Keep styling minimal.

Navbar:

```text
Admin
Connect
Upload
```

Show current connection state.

Use fetch API only.

No component library.

---

# Build Integration

Frontend build must happen automatically.

Maven should:

```text
npm install
npm run build
```

during:

```text
mvn package
```

Copy output:

```text
ui/dist/*
```

to:

```text
src/main/resources/META-INF/resources/app
```

Provide complete:

```text
pom.xml
```

configuration.

---

# Run

Development:

```bash
./mvnw quarkus:dev
```

Build:

```bash
./mvnw package
```

Access:

```text
http://localhost:8080/app
```

---

# Deliverables

Generate:

- complete project tree
- all source files
- complete pom.xml
- README
- runnable implementation
- comments showing where Savapage integration will later replace in-memory storage
- no tests
- no Docker
