plugins { id("com.android.application") }
android {
    namespace = "uy.quicktrip.wearqa"
    compileSdk = 35
    defaultConfig {
        applicationId = "uy.quicktrip.wearqa"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "0.2-bridge-qa"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
    testImplementation("junit:junit:4.13.2")
}
