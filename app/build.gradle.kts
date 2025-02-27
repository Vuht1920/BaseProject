import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import deps.dependOn
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.devtoolsKsp)
}

val versions = rootProject.file("version.properties")
val props = Properties()
props.load(FileInputStream(versions))
val major = props["majorVersion"].toString().toInt()
val minor = props["minorVersion"].toString().toInt()
val patch = props["patchVersion"].toString().toInt()

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply { load(FileInputStream(keystorePropertiesFile)) }

android {
    namespace = Build.applicationId
    compileSdk = Build.compileSdk

    defaultConfig {
        applicationId = Build.applicationId
        minSdk = Build.minSdk
        targetSdk = Build.targetSdk
        versionCode = 10000 * major + 1000 * minor + 10 * patch
        versionName = "$major.$minor.$patch"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("boolean", "SHOW_AD", "true")
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    flavorDimensions += "environment"
    productFlavors {
        create("App") {
            dimension = "environment"
            applicationId = Build.applicationId
            versionName = "0.001"
            versionCode = 1
            buildConfigField("boolean", "TEST_AD", "false")
        }
        create("App_Debug") {
            dimension = "environment"
            applicationId = Build.applicationId
            versionName = "0.001"
            versionCode = 1
            buildConfigField("boolean", "TEST_AD", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    bundle {
        language { enableSplit = false }
    }
    lint {
        abortOnError = false
    }
    // Tự động đổi tên apk và mapping file
    android.applicationVariants.all {
        val namePrefix = "BaseProject"
        val apkName = namePrefix + "_v${versionName}.apk"
        outputs.all {
            this as BaseVariantOutputImpl
            outputFileName = apkName
        }
    }
}

dependencies {

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Lifecycle process
    implementation(libs.androidx.lifecycle.process)

    //Splash
    implementation(libs.splashscreen)

    // Delegate View
    implementation(libs.view.binding.delegate)

    // Standard dependencies
    dependOn(
        deps.Core,
        deps.Hilt,
        deps.Navigation,
        deps.Log,
        deps.Glide,
        deps.Coroutine,
    )

    api(project(":features:ads"))
}