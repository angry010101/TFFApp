plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.tffapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tffapp"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    // Navigation library
    val nav_version = "2.3.5"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // CameraX core library
    val camerax_version = "1.1.0-beta03"
    implementation("androidx.camera:camera-core:$camerax_version")

    // CameraX Camera2 extensions
    implementation("androidx.camera:camera-camera2:$camerax_version")

    // CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:$camerax_version")

    // CameraX View class
    implementation("androidx.camera:camera-view:$camerax_version")

    //WindowManager
    implementation("androidx.window:window:1.0.0-alpha09")

    // Unit testing
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("androidx.test:rules:1.4.0")
    testImplementation("androidx.test:runner:1.4.0")
    testImplementation("androidx.test.espresso:espresso-core:3.4.0")
    testImplementation("org.robolectric:robolectric:4.4")

    // Instrumented testing
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.0")
    // Import the GPU delegate plugin Library for GPU inference
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.9.0")
}