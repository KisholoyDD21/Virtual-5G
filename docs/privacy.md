# Privacy

## What's collected, and why

| Data | Purpose | Stored where |
|---|---|---|
| Connection type, signal strength, measured speed/latency | Compute the on-device score and mode | Locally (Room); server only if you explicitly submit a speed test result |
| Device manufacturer/model, Android version | Compatibility decisions | Locally only, never transmitted |
| Plan info you enter or that the mock carrier provider returns | Show 5G eligibility | Locally, and server-side only if you're signed in and submit it |
| Speed test results | Show your history | Server, tied to your account if signed in, anonymous otherwise |
| Telemetry (aggregate usage events) | Improve the app | **Off by default.** Requires explicit opt-in; rejected server-side (`403`) if the account hasn't opted in, even with a valid token - see `backend/app/routers/telemetry.py` |

## Never collected, anywhere in this codebase

Message content, call logs, contacts, browsing history, decrypted HTTPS payloads,
or precise location. `CellularTelephonyObserver` reads signal strength and network
type only - it never touches `TelephonyManager` APIs related to calls, SMS, or
contacts, and the app requests no permission that would allow it to.

## Where consent is enforced, not just requested

- **Telephony permission**: gated behind `PermissionUtils` checks throughout
  `:data`; every telephony read degrades gracefully (returns `UNKNOWN`, never
  crashes) if denied. See the Onboarding screen's explicit "Skip for now" option.
- **Telemetry**: the Privacy Dashboard toggle is real, but the authoritative check
  is server-side (`User.telemetry_opt_in`) - a compromised or modified client can't
  submit telemetry for an account that hasn't opted in.
- **Plan verification source**: user-entered plan data is always labeled
  `user_provided` at the database level, in both the Android app and the backend -
  there's no code path that silently promotes it to `carrier_api`.

## Security measures in this codebase

- The one locally-persisted flag that matters for correctness (whether this device
  has ever been observed on 5G) is stored via `EncryptedSharedPreferences`
  (Android Keystore-backed), with a non-encrypted fallback only if Keystore
  initialization itself fails on a given device.
- `SecureLogger` (`:core`) redacts common sensitive field name patterns
  (tokens, IMEI/IMSI-shaped keys, passwords) before any log line is written, and
  suppresses debug-level logs entirely in release builds.
- Backend passwords are hashed with bcrypt (via the `bcrypt` package directly - see
  `docs/limitations.md` for why passlib was avoided) and auth uses short-lived JWTs.
- CORS is wide open (`allow_origins=["*"]`) in the provided `main.py` for local
  development convenience - **tighten this to specific origins before deploying
  anywhere reachable from the internet.**

See `docs/limitations.md` for what a full production security review would still
need to cover (this MVP has not had one).
