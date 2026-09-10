# Contributing to Virtual 5G

## Ground rules

1. **Never claim radio-level 5G where it isn't real.** Any PR that makes
   `VIRTUAL_5G` mode display or behave like `REAL_5G`, or that infers 5G hardware
   support from anything other than observed NR attachment, will be rejected. This
   is the one non-negotiable rule of the project - see `docs/limitations.md` for
   the reasoning.
2. **Document platform limitations, don't paper over them.** If you hit an Android
   or carrier API constraint, add it to `docs/limitations.md` with the decision you
   made, following the existing entries' format.
3. **New repository/provider implementations go behind existing interfaces.** A
   real carrier integration implements `CarrierProvider`
   (`domain/repository/CarrierProvider.kt` on Android, `app/providers/carrier_provider.py`
   on the backend) - it does not change call sites.

## Android

- Business logic (scoring, mode selection) belongs in `:domain` and must stay free
  of Android framework imports so it's unit-testable without a device.
- Code touching `android.telephony.*` belongs in `:data`. If it needs an API level
  above your change's minimum, isolate it in its own class rather than an `if
  (SDK_INT >= X)` branch inside a shared one - see `CellularTelephonyObserver` for
  the pattern and why it matters.
- Run `./gradlew :domain:test` before opening a PR that touches scoring or mode
  logic; add a test case for any new branch.

## Backend

- `pip install -r requirements.txt`, then `python3 -m pytest tests/ -v` - all tests
  must pass. Add a test for any new endpoint or behavior change.
- Keep `CarrierProvider` implementations swappable via the `get_carrier_provider`
  dependency - don't hardcode a provider into a router.

## Commit / PR style

- Small, focused PRs. Explain *why*, not just *what*, especially for anything
  touching mode selection or capability detection.
- Update `docs/limitations.md` or `docs/architecture.md` in the same PR if your
  change affects what they describe - stale docs are worse than no docs.
