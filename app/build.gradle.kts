plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "cc.jtogashi.mapboxnavsupporthelper"
    compileSdk = 35

    defaultConfig {
        applicationId = "cc.jtogashi.mapboxnavsupporthelper"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.car)
    implementation(libs.androidx.car.projected)

    implementation(libs.mapbox.navigationcore)
    implementation(libs.mapbox.navigationcore.auto)

    implementation(libs.mapbox.search)
    implementation(libs.mapbox.search.autofill)
    implementation(libs.mapbox.search.native)
    implementation(libs.mapbox.search.android.ui)

    implementation(libs.mapbox.maps.android.ndk27)
    implementation(libs.mapbox.navigation.ui.maps)
    implementation(libs.mapbox.navigation.ui.components)
    implementation(libs.mapbox.navigation.navigation)
    implementation(libs.mapbox.navigation.tripdata)
    implementation(libs.mapbox.navigation.voice)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}