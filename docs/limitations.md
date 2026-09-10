# Limitations and design decisions

This document exists because the spec for this project explicitly asked for it:
whenever a platform limitation prevented something, document it and implement the
closest legitimate alternative rather than faking it. This is the complete list.

## 1. There is no public API for "does this modem support 5G hardware"

Android has no non-privileged API that answers this as a standing fact independent
of current coverage. `TelephonyDisplayInfo` and `ServiceState` only expose what the
radio is doing *right now*. A 5G-capable phone sitting somewhere with no 5G coverage
looks identical, from the public API surface, to a phone that simply can't do 5G.

**Decision:** `DeviceCapabilityManager` treats 5G support as *confirmed by
observation* - the first time the device is actually seen attached via NR, a flag is
persisted (Keystore-backed) and never un-set. Before that, the UI shows "not yet
confirmed" rather than a hard "no". See `CapabilityConfidence` in the domain model
and the Device Compatibility screen, which explains this to the user directly rather
than hiding the uncertainty.

## 2. NR detection needs three separate code paths, not one

`TelephonyDisplayInfo` (the type that reports NSA/Advanced 5G) was added in API 30.
The callback mechanism to observe it changed again in API 31 (`PhoneStateListener` →
`TelephonyCallback`). Below API 30, standalone (SA) 5G is only detectable via
`TelephonyManager.dataNetworkType == NETWORK_TYPE_NR` (API 29+); below API 29, NR is
not detectable via public API at all.

**Decision:** `CellularTelephonyObserver` dispatches to three genuinely separate
classes (`ModernTelephonyCallbackSource`, `DisplayInfoPhoneStateListenerSource`,
`SignalOnlyPhoneStateListenerSource`) selected once at construction time by
`Build.VERSION.SDK_INT`, rather than one class with runtime `if` branches referencing
API-30/31-only types. This avoids a real, known Android verifier pitfall where a
class referencing a newer API in a method signature can cause problems when loaded
on an older OS, even inside a guarded branch.

## 3. No supported API enumerates "all radios this modem supports"

There's no non-privileged call that lists every RAT (radio access technology) a
modem is capable of, independent of the current cell.

**Decision:** `supportedNetworkTypeNames` only ever claims what's been directly
observed (LTE assumed present on any telephony-capable device; NR added only once
seen), never inferred from device model or marketing specs.

## 4. Per-cell signal dBm isn't available on every API level

`SignalStrength.getCellSignalStrengths()` (the API that gives real per-cell dBm) was
added in API 29. Below that, only the normalized 0-4 `getLevel()` is available.

**Decision:** `extractDbm()` returns `null` on API < 29 rather than fabricating a
dBm value from the level. The UI shows "—" for signal dBm on old devices instead of
a fake number.

## 5. VpnService-based "network acceleration" was deliberately not built

The spec asked us to investigate whether `VpnService` could provide legitimate
optimization, and was explicit that it must never decrypt HTTPS, perform MITM, or
bypass carrier restrictions. Once those are off the table, a VPN service has two
honest options: (a) a pass-through tunnel that doesn't meaningfully optimize
third-party apps' traffic, since it can't see inside encrypted payloads to do
anything useful, or (b) a DNS-changing VPN, which is a real, legitimate use case but
is a different, narrower feature than "network acceleration."

**Decision:** No system-wide VPN is implemented in this MVP. The optimizations that
*are* implemented (DNS-over-HTTPS-aware resolution, endpoint selection, caching,
compression) all operate inside the app's own network client (OkHttp), which is
honest about its scope - it can only ever help this app's own traffic, and the UI
never claims otherwise. A DNS-changing `VpnService` is a reasonable, scoped future
addition; a "traffic accelerating" one is not achievable honestly with public APIs.

## 6. No real carrier integration

Section 4 of the spec explicitly forbids scraping carrier websites. A production
`CarrierProvider` implementation requires a commercial agreement with a specific
carrier's API - there's no generic public one to integrate against.

**Decision:** `MockCarrierProvider` (Android) and `MockCarrierProvider` (backend)
implement the exact same interface a real integration would, with fixture data
matching the spec's example JSON. Manual/user-provided plan entry is fully
implemented and always labeled `USER_PROVIDED`, never silently upgraded to
carrier-verified status.

## 7. Multi-SIM / dual-SIM is out of scope for this MVP

`CellularTelephonyObserver` reads the default `TelephonyManager` instance rather than
per-subscription instances via `TelephonyManager.createForSubscriptionId()`. On a
dual-SIM device, this reflects one SIM's state, not both.

**Decision:** Documented here rather than silently wrong. `SubscriptionManager`
integration for true multi-SIM support is a clearly scoped next step.

## 8. What's implemented vs. scaffolded

Fully implemented, real logic: `:domain` (scoring, mode selection, use cases) and
`:data` (telephony detection, speed test engine, mock carrier provider) are complete
and unit-tested where they don't require an Android device. The 13 screens from the
spec were consolidated into 10 (Onboarding absorbs Permission Setup; Virtual 5G
Settings absorbs Optimization Controls; Diagnostics absorbs Network Analytics) -
every consolidation is a UI grouping decision, not a dropped feature.

Not compiled/verified: the Android app was written in a sandbox without Android SDK
access, so while every API call, permission, and Gradle coordinate was checked
against current documentation, it has not been through an actual `./gradlew build`.
Expect to fix minor issues (an import, a version mismatch) on first sync in Android
Studio. The backend, by contrast, **was** actually installed, run, and tested end to
end in this environment (12 passing pytest tests covering auth, plan verification,
speed test submission, and telemetry consent enforcement) - see `backend/tests/`.

Not built: Alembic migrations (schema is created via `create_all` for MVP
simplicity), full CI matrix, and a design pass for a light theme (this app is
dark-only by product decision, not oversight - see `presentation/theme/Theme.kt`).
