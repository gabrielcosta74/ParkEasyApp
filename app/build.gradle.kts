plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "pt.ubi.pdm.parkeasyapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "pt.ubi.pdm.parkeasyapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // === BuildConfig fields (lidos do gradle.properties) ===
        val supabaseUrl = project.findProperty("SUPABASE_URL_DEV") as String? ?: ""
        val supabaseAnon = project.findProperty("SUPABASE_ANON_KEY_DEV") as String? ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnon\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // útil para logs de rede
            isMinifyEnabled = false
        }
    }

    // Usa Java 17 (recomendado para AGP 8.x)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            // evita conflitos ocasionais com licenças em libs
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.fragment)
    implementation(libs.recyclerview)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)

    // Localização (GPS)
    implementation(libs.play.services.location)

    // Rede (Retrofit + OkHttp + Gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Imagens (Glide)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
