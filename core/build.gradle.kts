plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

android {
    namespace = "app.aromas.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.all { it.useJUnitPlatform() }
    }

    // Shared test builders (aroma()) exposed to :app tests via test fixtures.
    testFixtures {
        enable = true
    }
}

kover {
    reports {
        verify {
            rule("At least 80 % line coverage in :core") {
                minBound(80)
            }
        }
        filters {
            excludes {
                // @Serializable DTOs (+ generated serializers) carry no logic and
                // add synthetic branches; the logic lives testably in data/ + logic/.
                packages("app.aromas.core.model")
                classes("*\$serializer", "*_Impl*", "_*")
            }
        }
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(testFixtures(project(":core")))
    testRuntimeOnly(libs.junit.platform.launcher)
}
