plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  id("com.google.gms.google-services")
  id("kotlin-kapt")

}

android {
  namespace = "com.openclassrooms.hexagonal.games2"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.openclassrooms.hexagonal.games2"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.11"
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    compose = true
  }
  testOptions {
    unitTests.isIncludeAndroidResources = true
  }

}

dependencies {

  // Testing
  testImplementation("com.google.dagger:hilt-android-testing:2.50")
  testImplementation("androidx.arch.core:core-testing:2.2.0")
  testImplementation("org.mockito:mockito-core:4.2.0")
  testImplementation ("org.mockito:mockito-inline:4.2.0")
  testImplementation("org.mockito:mockito-junit-jupiter:3.12.4")
  testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
  testImplementation("io.mockk:mockk:1.13.3")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")


  // glide
  implementation("io.coil-kt:coil-compose:2.6.0")
  implementation ("com.github.bumptech.glide:glide:4.15.1")
  kapt ("com.github.bumptech.glide:compiler:4.15.1")


  //firebase firestore

  implementation("com.google.firebase:firebase-auth-ktx:23.1.0")
  implementation("com.google.firebase:firebase-storage-ktx:21.0.1")

  // Firebase Firestore SDK
  implementation ("com.google.firebase:firebase-firestore:25.1.1")

  // FirebaseUI Firestore SDK
  implementation ("com.firebaseui:firebase-ui-firestore:8.0.0")

  // Firebase Core SDK
  implementation ("com.google.firebase:firebase-core:21.1.1")

  // Firebase Storage SDK
  implementation ("com.google.firebase:firebase-storage:21.0.1")

  // FirebaseUI Storage SDK
  implementation ("com.firebaseui:firebase-ui-storage:8.0.0")



  implementation ("com.google.firebase:firebase-messaging:24.1.0")  // Dernière version de FCM
  implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
  implementation("com.google.firebase:firebase-analytics")
  implementation("com.google.firebase:firebase-auth")



  //kotlin
  implementation(platform(libs.kotlin.bom))

  //DI
  implementation(libs.hilt)
  implementation(libs.firebase.firestore.ktx)
  ksp(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)

  //compose
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.graphics)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.material)
  implementation(libs.compose.material3)
  implementation(libs.lifecycle.runtime.compose)
  debugImplementation(libs.compose.ui.tooling)
  debugImplementation(libs.compose.ui.test.manifest)

  implementation(libs.activity.compose)
  implementation(libs.navigation.compose)

  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.coil.compose)
  implementation(libs.accompanist.permissions)

  testImplementation(libs.junit)
  androidTestImplementation(libs.ext.junit)
  androidTestImplementation(libs.espresso.core)
}