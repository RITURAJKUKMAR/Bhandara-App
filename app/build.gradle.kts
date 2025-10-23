plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.rituraj.bhandaraapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rituraj.bhandaraapp"
        minSdk = 24
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
    packagingOptions.resources.excludes.add("META-INF/INDEX.LIST")
    packagingOptions.resources.excludes.add("META-INF/*.SF")
    packagingOptions.resources.excludes.add("META-INF/*.DSA")
    packagingOptions.resources.excludes.add("META-INF/*.RSA")
    packagingOptions.resources.excludes.add("META-INF/DEPENDENCIES")
//    packaging {
//        resources {
//            excludes += ['META-INF/INDEX.LIST', 'META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA']
//        }
//    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.firebase.ai)
    implementation(libs.legacy.support.v4)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.navigation.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    // ✅ Firebase BOM (versions automatic handle honge)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")

    implementation("com.google.android.gms:play-services-location:21.3.0") // for location

    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0") // for google

    // Image handling
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // for notifications
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("com.google.firebase:firebase-analytics")
}