plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "unitauto.apk"
    compileSdk = 36

    defaultConfig {
        minSdk = 29

        consumerProguardFiles("consumer-rules.pro")
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

    packaging {
        resources {
            excludes.add("**/application.properties")
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.fastjson)
    implementation(libs.androidasync)
    // 本地或远程依赖，二选一
    implementation(libs.unitauto)
}