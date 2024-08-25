plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.codingchallangemovieapp"
    compileSdk = 34

    val API_KEY: String by project

    defaultConfig {
        applicationId = "com.example.codingchallangemovieapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_KEY", "\"$API_KEY\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.iconics.core)
    implementation(libs.mikepenz.iconics.views)
    implementation(libs.fontawesome.typeface)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
//    implementation("com.mikepenz:fontawesome-typeface:5.9.0.3-kotlin@aar")
}