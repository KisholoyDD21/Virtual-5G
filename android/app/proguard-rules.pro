# Room, OkHttp, and WorkManager ship their own consumer rules via AAR
# metadata, so nothing extra is required for them here.

# Keep domain model classes intact - they're small, not perf-sensitive, and
# keeping them readable in stack traces from crash reports is worth the
# negligible size cost.
-keep class com.virtual5g.domain.model.** { *; }

# Kotlin coroutines / Room generated code occasionally trips R8 without this.
-dontwarn kotlinx.coroutines.**
