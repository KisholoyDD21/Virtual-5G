# API reference

Base URL (local dev): `http://localhost:8000`. Interactive docs at `/docs`
(Swagger UI) and `/redoc` once the server is running - both are generated
automatically by FastAPI from the same schemas documented here.

## Auth

### `POST /auth/register`
Body: `{"email": string, "password": string (min 8 chars)}`
→ `201` `{"id", "email", "telemetry_opt_in"}` · `409` if email already registered.

### `POST /auth/login`
Form-encoded (`application/x-www-form-urlencoded`): `username`, `password`
(OAuth2 password flow field names - `username` carries the email).
→ `200` `{"access_token", "token_type": "bearer"}` · `401` on bad credentials.

All routes below except `/health` and `/speedtest/config` require
`Authorization: Bearer <access_token>` unless noted otherwise.

## Plan

### `GET /plan`
Returns the carrier-sourced plan (mock provider in this MVP).
→ `200` `{"carrier_name", "plan_name", "five_g_eligible", "expiry", "verification_source"}`

### `POST /plan/manual`
Body: `{"carrier_name", "plan_name", "five_g_eligible", "expiry"?}`
Always stored with `verification_source: "user_provided"` - never upgraded to
carrier-verified status server-side.
→ `201` same shape as `GET /plan`.

## Speed test

### `GET /speedtest/config`
No auth required. Returns the current test endpoints and per-tier byte budgets, so
the fleet can be pointed at different infrastructure without an app release.
→ `200` `{"download_url", "upload_url", "quick_download_bytes", "standard_download_bytes", "deep_download_bytes"}`

### `POST /speedtest/results`
Auth optional - works anonymously since running a local test doesn't require an
account. Body: `{"tier", "download_mbps"?, "upload_mbps"?, "ping_ms"?, "jitter_ms"?, "packet_loss_percent"?}`
→ `201` the stored record including `id` and `created_at`.

## Telemetry

### `POST /telemetry`
Requires auth **and** `telemetry_opt_in = true` on the account - rejected with
`403` otherwise, even with a valid token. This is enforced server-side, not just
trusted from the client. Body: `{"event_type", "operating_mode"?, "network_score"? (0-100)}`
→ `201` `{"accepted": true}`

## Health

### `GET /health`
No auth required.
→ `200` `{"status": "ok", "environment": "development" | "production"}`
