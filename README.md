# UnniAttach – Student Attachment Management System (REST API)

Spring Boot 3 + MySQL backend for managing student industrial attachments.

**Roles:** `STUDENT`, `COMPANY`, `SUPERVISOR` (university), `ADMIN`

**How it works**

1. A **company** posts a placement (title, slots, dates, deadline).
2. A **student** applies. The **company** accepts or rejects.
3. Accepting creates an **attachment**. When all slots are filled the placement closes itself.
4. An **admin** assigns a university **supervisor** to the attachment.
5. The student writes **logbook** entries; the company and the supervisor comment on them.
6. The company and the supervisor each give an **assessment** score (0-100).
7. The company (or admin) marks the attachment **completed**.

Students and companies can self-register. Supervisor accounts are created by an admin, and the first admin is created from environment variables.

---

## Run locally

Requirements: JDK 17+, MySQL running on your machine.

1. Copy `src/main/resources/application-local.properties.example` to
   `src/main/resources/application-local.properties` and fill in your MySQL password. This file is gitignored.
2. Start the app with the `local` profile.

   PowerShell:
   ```powershell
   $env:SPRING_PROFILES_ACTIVE="local"
   .\mvnw.cmd spring-boot:run
   ```
   Mac/Linux:
   ```bash
   SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
   ```
3. Open **http://localhost:8080/swagger-ui.html**. Log in via `POST /api/auth/login`, click **Authorize**, paste the token.

Run the tests (uses an in-memory H2 database, no MySQL needed):

```bash
./mvnw verify
```

## Environment variables

| Variable | Required | Purpose |
|---|---|---|
| `JWT_SECRET` | **yes** | Random string, 32+ characters. The app will not start without it. |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | recommended | Creates the first admin on startup (password 8+ chars). |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | one DB option | Full JDBC URL, e.g. `jdbc:mysql://host:3306/attaco` |
| `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD` | alternative | Railway-style MySQL variables |
| `CORS_ALLOWED_ORIGINS` | if you have a frontend | Comma-separated origins, e.g. `https://my-site.vercel.app` |
| `JWT_EXPIRATION_MINUTES` | no | Default 1440 (24 hours) |
| `PORT` | no | Default 8080 (hosts set this automatically) |

## Deploy to Railway (Spring Boot + MySQL)

1. Push this repo to GitHub.
2. On railway.com create a **New Project -> Deploy from GitHub repo** and pick this repo. The `Dockerfile` is detected automatically.
3. In the same project click **+ New -> Database -> MySQL**.
4. Open the web service -> **Variables** and add (the `MySQL` part is the name of your database service):

   ```
   MYSQLHOST=${{MySQL.MYSQLHOST}}
   MYSQLPORT=${{MySQL.MYSQLPORT}}
   MYSQLDATABASE=${{MySQL.MYSQLDATABASE}}
   MYSQLUSER=${{MySQL.MYSQLUSER}}
   MYSQLPASSWORD=${{MySQL.MYSQLPASSWORD}}
   JWT_SECRET=<a long random string>
   ADMIN_EMAIL=you@example.com
   ADMIN_PASSWORD=<a strong password>
   CORS_ALLOWED_ORIGINS=https://your-frontend-url
   ```
5. **Settings -> Networking -> Generate Domain**. Optionally set the health check path to `/actuator/health`.
6. Visit `https://<your-domain>/swagger-ui.html`.

Tables are created automatically on first start (`ddl-auto=update`).

## Endpoints

All endpoints except register, login, health and Swagger need `Authorization: Bearer <token>`.

| Method | Path | Who |
|---|---|---|
| POST | `/api/auth/register` | anyone (STUDENT or COMPANY) |
| POST | `/api/auth/login` | anyone |
| GET | `/api/auth/me` | any logged-in user |
| GET | `/api/placements?q=` | any logged-in user (open placements) |
| GET | `/api/placements/{id}` | any logged-in user |
| GET | `/api/placements/mine` | COMPANY |
| POST | `/api/placements` | COMPANY |
| PUT | `/api/placements/{id}` | owner COMPANY, ADMIN |
| PATCH | `/api/placements/{id}/close` | owner COMPANY, ADMIN |
| GET | `/api/placements/{id}/applications` | owner COMPANY, ADMIN |
| POST | `/api/applications` | STUDENT |
| GET | `/api/applications/mine` | STUDENT |
| PATCH | `/api/applications/{id}/withdraw` | STUDENT |
| PATCH | `/api/applications/{id}/decision` | owner COMPANY (`ACCEPTED` / `REJECTED`) |
| GET | `/api/attachments` | each role sees its own, ADMIN sees all |
| GET | `/api/attachments/{id}` | people on that attachment, ADMIN |
| PATCH | `/api/attachments/{id}/supervisor` | ADMIN |
| PATCH | `/api/attachments/{id}/complete` | owner COMPANY, ADMIN |
| POST / GET | `/api/attachments/{id}/logbook` | STUDENT writes; everyone on the attachment reads |
| PUT | `/api/logbook/{entryId}` | STUDENT (until reviewed) |
| PATCH | `/api/logbook/{entryId}/review` | COMPANY, assigned SUPERVISOR |
| POST / GET | `/api/attachments/{id}/assessments` | COMPANY / SUPERVISOR write; everyone on it reads |
| GET | `/api/admin/users?role=` | ADMIN |
| POST | `/api/admin/users` | ADMIN (create supervisors) |
| PATCH | `/api/admin/users/{id}/enabled` | ADMIN |
| GET | `/api/admin/stats` | ADMIN |
