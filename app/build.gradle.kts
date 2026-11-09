plugins {
 id("com.android.application")
 kotlin("android")
 kotlin("plugin.serialization")
 id("org.jetbrains.kotlin.plugin.compose")
}
android {
 namespace = "com.crimegrid"
 compileSdk = 35
 buildToolsVersion = "35.0.0"
 defaultConfig { applicationId = "com.crimegrid"; minSdk = 26; targetSdk = 35; versionCode = 5; versionName = "0.5" }
 buildFeatures { compose = true }
 testOptions { unitTests.isIncludeAndroidResources = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
 kotlinOptions { jvmTarget = "21" }
 packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
dependencies {
 implementation(project(":core"))
 testImplementation("junit:junit:4.13.2")
 testImplementation("org.robolectric:robolectric:4.14.1")
 testImplementation("androidx.compose.ui:ui-test-junit4")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.9.3")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui-tooling-preview")
 debugImplementation("androidx.compose.ui:ui-tooling")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
