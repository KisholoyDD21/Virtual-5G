// Top-level build file. Plugin versions are declared here (apply false) and
// applied per-module so every module stays on the same toolchain version.
//
// Note on DI: this project uses a manual composition root (see
// app/src/main/java/com/virtual5g/app/di/AppContainer.kt) rather than
// Hilt/Dagger. Everything is still built against interfaces defined in
// :domain, so swapping in Hilt later only touches the composition root and
// a few `@HiltViewModel` annotations - nothing in :data or :presentation
// would need to change. See docs/architecture.md for the rationale.
plugins {
    id("com.android.application") version "8.13.0" apply false
    id("com.android.library") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    id("com.google.devtools.ksp") version "2.2.20-2.0.4" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
