# Virtual 5G

[![CI](https://github.com/KisholoyDD21/Virtual-5G/actions/workflows/ci.yml/badge.svg)](https://github.com/KisholoyDD21/Virtual-5G/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Python](https://img.shields.io/badge/Python-3.13-3776AB.svg?logo=python&logoColor=white)](https://www.python.org/)

A software-optimized "5G-like" connectivity experience on 4G devices, which
automatically steps aside for real 5G the moment your hardware, network, and plan
actually support it.

> **Virtual 5G does not convert 4G hardware into a real 5G modem.** It provides a
> software-level optimized connectivity experience on 4G devices, while genuine 5G
> functionality is enabled only when the underlying hardware and network support it.

## The problem

Most phones in most markets today are 4G, but users increasingly have 5G-branded
recharge plans, and the gap between "what my plan promises" and "what my phone can
actually do" is confusing and, in a lot of apps, quietly misrepresented. This
project takes the opposite approach: be precise about what's real (radio-level 5G,
detected honestly) versus what's software (caching, DNS/endpoint optimization,
adaptive behavior), and make that distinction visible in the UI rather than hiding
it behind a marketing label.

## Solution

- **4G-only device** → *Virtual 5G Active*: software-level optimizations run, no
  radio-level claim is made.
- **5G-capable device, actually attached to 5G, with an eligible plan** → *5G
  Connected*: real 5G, and only this state unlocks 5G-dependent features.
- **5G-capable device, no eligible plan (or out of coverage)** → Optimized 4G /
  Virtual 5G, with an explanation of exactly what's missing and why.
- **4G-only device with a 5G-eligible plan** → *5G Plan Detected — Device Limited to
  4G*: the plan is acknowledged, but the hardware ceiling is stated plainly.

The full decision logic lives in `OperatingModeSelector`
(`android/domain/src/main/java/com/virtual5g/domain/engine/OperatingModeSelector.kt`)
and is unit tested for all four scenarios above.

## Architecture

Five-module Clean Architecture Android app (`domain` → `core` → `data`/`presentation`
→ `app`) plus a FastAPI backend for optional account/telemetry/plan services. Full
diagram and rationale in [`docs/architecture.md`](docs/architecture.md).

```
virtual-5g/
├── android/            Kotlin + Jetpack Compose, 5 Gradle modules
├── backend/             FastAPI + SQLAlchemy, tested (12 passing pytest tests)
├── docs/                 architecture, API reference, privacy, limitations
└── docker-compose.yml   backend + Postgres + Redis
```

## Technology

**Android**: Kotlin, Jetpack Compose, Material 3, Coroutines/Flow, Room, WorkManager,
Navigation Compose, OkHttp. AGP 8.13, Kotlin 2.2.20, Compose BOM 2026.06.00,
compileSdk/targetSdk 36, minSdk 26.

**Backend**: FastAPI, SQLAlchemy 2, Pydantic v2, PostgreSQL (Docker) / SQLite (local
dev), Redis, Docker.

## Features

- Real-time dashboard: connection card, device card, plan card, optimization card,
  and a 0–100 Virtual 5G Experience Score with a five-metric breakdown.
- Honest device-capability detection (see [`docs/limitations.md`](docs/limitations.md)
  for exactly why this is "confirmed by observation" rather than a hard yes/no).
- Configurable-tier speed test (Quick/Standard/Deep) with real DNS/TCP/TLS/download/
  upload timing via OkHttp's `EventListener`.
- Mock carrier provider (Android + backend) matching a real integration's shape, plus
  full manual plan entry, always labeled `user_provided`.
- Privacy dashboard with real, not decorative, toggles - telemetry consent is
  enforced server-side, not just in the UI.
- Offline mode: last known measurement is cached (Room) and shown with a relative
  timestamp when there's no connection.

## Screenshots

Captured from the app running on a physical Android 15 device.

<p>
  <img src="https://github.com/KisholoyDD21/Virtual-5G/blob/main/screenshots/dashboard.png?raw=true" alt="Virtual 5G dashboard" width="320" />
  <img src="https://github.com/KisholoyDD21/Virtual-5G/blob/main/screenshots/device-compatibility.png?raw=true" alt="Device compatibility" width="320" />
</p>
<p>
  <img src="https://github.com/KisholoyDD21/Virtual-5G/blob/main/screenshots/speed-test.png?raw=true" alt="Speed test" width="320" />
</p>

## Installation

### Android

```bash
cd android
# Open in Android Studio (Ladybug or newer, JDK 17) and let it sync, or:
./gradlew assembleDebug
# Windows PowerShell:
# .\gradlew.bat assembleDebug
```

The project is pinned to Gradle 8.13 and includes the Gradle wrapper. A connected
Android device or emulator is required for installation and instrumented tests.

### Backend

```bash
cd backend
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload
# → http://localhost:8000/docs
```

The backend test suite can be run with:

```bash
python3 -m pytest tests/ -v
```

### Full stack via Docker

```bash
docker compose up --build
# backend on :8000, Postgres and Redis as declared in docker-compose.yml
```

### Environment variables

See [`backend/.env.example`](backend/.env.example) for the full list
(`DATABASE_URL`, `REDIS_URL`, `JWT_SECRET_KEY`, speed test endpoints, telemetry
default). None of the example values are safe for a real deployment.

## API documentation

See [`docs/api.md`](docs/api.md), or run the backend and visit `/docs` for the
live, generated Swagger UI.

## Testing

- **Android unit tests** (`:domain`, no device needed): `./gradlew :domain:test` -
  covers network scoring and every operating-mode branch, including both Virtual 5G
  sub-cases (plain 4G device, and 4G device with a 5G-eligible plan).
- **Android instrumented tests**: `./gradlew connectedAndroidTest` - one sample
  suite included (onboarding flow); see `docs/limitations.md` for the rest of the
  suite's scope.
- **Backend**: `python3 -m pytest tests/ -v` - 12 tests across auth, plan
  verification, speed test, and telemetry consent enforcement, all passing.

## Privacy & security

See [`docs/privacy.md`](docs/privacy.md) and [`docs/limitations.md`](docs/limitations.md).

## Limitations & roadmap

Every platform constraint this project ran into, and the decision made in response,
is documented in full in [`docs/limitations.md`](docs/limitations.md) rather than
glossed over. Short version of what's next: Alembic migrations, multi-SIM support,
a real carrier integration behind the existing `CarrierProvider` interface, and full
instrumented-test coverage across every screen.

## License

MIT - see [`LICENSE`](LICENSE).
