plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    alias(libs.plugins.google.gms.google.services) // Correct format
}

android {
    namespace = "com.codingempire.adminpse"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.codingempire.adminpse"
        minSdk = 24
        targetSdk = 36
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
    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    val nav_version = "2.7.7"
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.2")
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.converter.gson)
    implementation ("com.opencsv:opencsv:5.7.1")

    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.circleimageview)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.google.auth.library.oauth2.http)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.core)
    implementation(libs.lottie)


    configurations.all {
        resolutionStrategy {
            force("io.grpc:grpc-core:1.57.2")
            force("io.grpc:grpc-okhttp:1.57.2")
            force("io.grpc:grpc-api:1.57.2")
            force("io.grpc:grpc-stub:1.57.2")
            force("io.grpc:grpc-context:1.57.2")
            force("io.grpc:grpc-protobuf-lite:1.57.2")
        }
    }
}