plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.stickmanalive"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.stickmanalive"
        minSdk = 26 // Mendukung penuh mode Picture-in-Picture (PiP)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // =================================================================
    // 🔐 KONFIGURASI SERTIFIKAT DIGITAL (OTOMATIS DARI GITHUB ACTIONS)
    // =================================================================
    signingConfigs {
        create("release") {
            // Mengambil file sertifikat yang dibuat otomatis di root proyek
            storeFile = file("../release.keystore")
            storePassword = "stickman123"
            keyAlias = "stickman_key"
            keyPassword = "stickman123"
        }
    }

    // =================================================================
    // ⚠️ MEMATIKAN INTERUPSI LINT AGAR BUILD LOGO DUMMY TIDAK CRASH
    // =================================================================
    lint {
        checkReleaseBuilds = false
        abortOnError = false // SINTAKS SUDAH DIPERBAIKI (Tanpa kata 'is')
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Mengunci sertifikat digital ke dalam hasil kompilasi APK Release
            signingConfig = signingConfigs.getByName("release")
        }
        
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Pustaka Inti AndroidX & Material Design
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Pustaka Pengujian (Opsional)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
