plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.parcelize") // Parcelize plugin

}

android {
    namespace = "com.muneeb.sonara"
    compileSdk = 35

    viewBinding {
        enable = true
    }

    defaultConfig {
        applicationId = "com.muneeb.sonara"
        minSdk = 29
        targetSdk = 34
        versionCode = 3
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    android {
        // Existing configurations

        buildTypes {
            release {
                isMinifyEnabled = true  // This shrinks and obfuscates Java/Kotlin code
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                ndk {
                    debugSymbolLevel = "FULL"
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.filament.android)
    testImplementation(libs.junit)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.airbnb.android:lottie:6.5.0")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation("androidx.transition:transition:1.5.1")
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.media:media:1.7.0")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.android.exoplayer:extension-mediasession:2.19.1")
    implementation("androidx.core:core:1.13.1")
    implementation("jp.wasabeef:recyclerview-animators:4.0.2")
    implementation("io.github.everythingme:overscroll-decor-android:1.1.1")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
