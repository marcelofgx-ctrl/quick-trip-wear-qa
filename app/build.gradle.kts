plugins { id("com.android.application") }
android {
    namespace = "uy.vuelos.uruguay"
    compileSdk = 35
    defaultConfig {
        applicationId = "uy.vuelos.uruguay"
        minSdk = 26
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
