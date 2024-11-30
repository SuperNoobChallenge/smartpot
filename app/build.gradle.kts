import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.smartpot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smartpot"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties 파일 로드
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        // API_KEY와 DATABASE_URL 가져오기
        val apiKey = localProperties.getProperty("API_KEY") ?: ""
        val databaseUrl = localProperties.getProperty("DATABASE_URL") ?: ""

        // BuildConfig에 추가
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        buildConfigField("String", "DATABASE_URL", "\"$databaseUrl\"")
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

    buildFeatures {
        buildConfig = true  // buildConfig 기능 활성화
        viewBinding = true
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
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.android.identity:identity-credential-android:20231002")
    implementation("androidx.databinding:viewbinding:8.2.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.kakao.sdk:v2-user:2.15.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.navercorp.nid:oauth:5.3.0")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))

}
