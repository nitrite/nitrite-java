plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.dizitart.example.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.dizitart.example.android"
        // See MinSdk.md beside this file. Not a guess and not yet a measurement either — the
        // number is what the artifacts on the compile path require; PLAN.md M3 asks for a run.
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // dbinspect-bridge is Java 11 and uses java.time and java.nio; below API 26 those are
        // only there through desugaring, which is why this is on rather than assumed away.
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildTypes {
        release {
            // Deliberately off. THREAT-MODEL §7 criterion 2 must hold because the bridge is not
            // in the variant at all, not because R8 happened to shrink it away — R8 is
            // configurable away and is not the guard (see DbInspect#bridgeEnabled).
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("org.dizitart:nitrite:5.0.0")
    implementation("org.dizitart:nitrite-mvstore-adapter:5.0.0")

    // THE release guard on Android. An application has no command line, so the system property
    // -Ddbinspect.bridge.enabled can never be set and a guard requiring it could never be
    // satisfied; the packaging is the guard instead, and it is a stronger property than a runtime
    // flag rather than a weaker one. src/release/ carries a no-op DebugTools so that nothing in
    // the release variant so much as names a bridge class.
    debugImplementation("org.dizitart:nitrite-bridge:5.0.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
