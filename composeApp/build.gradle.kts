import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.Instant

val APP_NAME = "Kreate"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.room)
    alias(libs.plugins.hilt)
    //alias(libs.plugins.conveyor)
}

repositories {
    google()
    mavenCentral()
    //mavenLocal()
    maven { url = uri("https://jitpack.io") }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xcontext-receivers")
        }
    }

    jvm("desktop")



    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.desktop.currentOs)

            implementation(libs.material.icon.desktop)
            implementation(libs.vlcj)

            implementation(libs.coil.network.okhttp)
            runtimeOnly(libs.kotlinx.coroutines.swing)

            /*
            // Uncomment only for build jvm desktop version
            // Comment before build android version
            configurations.commonMainApi {
                exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
            }
            */





        }

        androidMain.dependencies {
            implementation(libs.navigation)
            implementation(libs.media3.session)
            implementation(libs.kotlin.coroutines.guava)
            implementation(libs.newpipe.extractor)
            implementation(libs.nanojson)
            implementation(libs.androidx.webkit)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(projects.innertube)
            implementation(projects.piped)
            implementation(projects.invidious)

            implementation(libs.room)
            implementation(libs.room.runtime)
            implementation(libs.room.sqlite.bundled)

            implementation(libs.mediaplayer.kmp)

            implementation(libs.navigation.kmp)

            //coil3 mp
            implementation(libs.coil.compose.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.mp)

            implementation(libs.translator)

        }
    }
}

android {
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileSdk = 35

    defaultConfig {
        applicationId = "it.fast4x.rimusic"
        minSdk = 21
        targetSdk = 35
        versionCode = 84
        versionName = "0.7.1"

        /*
                UNIVERSAL VARIABLES
         */
        val buildTime = Instant.now().toString()
        buildConfigField( "String", "BUILD_TIME", "\"$buildTime\"" )

        buildConfigField( "String", "APP_NAME", "\"$APP_NAME\"" )
    }

    splits {
        abi {
            reset()
            isUniversalApk = true
        }
    }

    namespace = "it.fast4x.rimusic"

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-kbm"
        }

        create( "full" ) {
            // App's properties
            applicationIdSuffix = ".kbuild"
            versionNameSuffix = "-kbf"
        }

        create( "minified" ) {
            // App's properties
            applicationIdSuffix = ".kbuild"
            versionNameSuffix = "-kbm"

            // Package optimization
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        /**
         * For convenience only.
         * "Folkers" want to change app name across builds
         * just need to change this variable
         */
        forEach {
            it.manifestPlaceholders["appName"] = APP_NAME
        }
    }

    applicationVariants.all {
        outputs.map { it as BaseVariantOutputImpl }
               .forEach { output ->
                   output.outputFileName = "$APP_NAME-$flavorName-${buildType.name}-unsigned.apk"
               }
    }

    //<editor-fold desc="Kbuild dimension">
    flavorDimensions += "kbuild"
    productFlavors {
        create( "upstream" ) {
            dimension = "kbuild"
            applicationIdSuffix += ".upstream"
        }
        create( "improvised" ) {
            dimension = "kbuild"
        }
    }
    //</editor-fold>

    sourceSets.all {
        kotlin.srcDir("src/$name/kotlin")
    }



    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

//    composeOptions {
//        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
//    }

    androidResources {
        generateLocaleConfig = true
    }
}



java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

compose.desktop {
    application {

        mainClass = "MainKt"


        //conveyor
        version = "0.0.1"
        group = "rimusic"
/*

        nativeDistributions {
            vendor = "fast4x RiMusic"
            description = "Desktop music player"
        }
        */

        //jpackage
        nativeDistributions {
            //conveyor
            vendor = "RiMusic.DesktopApp"
            description = "RiMusic Desktop Music Player"

            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "RiMusic.DesktopApp"
            packageVersion = "0.0.1"

            /*
            val iconsRoot = project.file("desktop-icons")
            windows {
                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
            }
            macOS {
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
            }
            linux {
                iconFile.set(iconsRoot.resolve("icon-linux.png"))
            }

             */
        }

    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {

    listOf(
        "kspAndroid",
         "ksp",
        //"kspIosSimulatorArm64",
        //"kspIosX64",
        //"kspIosArm64"
    ).forEach {
        add(it, libs.room.compiler)
    }

}

dependencies {
    implementation(libs.compose.activity)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ripple)
    implementation(libs.compose.shimmer)
    implementation(libs.compose.coil)
    implementation(libs.palette)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.appcompat)
    implementation(libs.appcompat.resources)
    implementation(libs.media)
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.compose.ui.graphics.android)
    implementation(libs.constraintlayout)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.animation)
    implementation(libs.kotlin.csv)
    implementation(libs.monetcompat)
    implementation(libs.androidmaterial)
    implementation(libs.timber)
    implementation(libs.crypto)
    implementation(libs.logging.interceptor)
    implementation(libs.math3)
    implementation(libs.toasty)
    implementation(libs.androidyoutubeplayer)
    implementation(libs.glance.widgets)
    implementation(libs.kizzy.rpc)
    implementation(libs.gson)
    implementation (libs.hypnoticcanvas)
    implementation (libs.hypnoticcanvas.shaders)

    implementation(libs.room)
    ksp(libs.room.compiler)

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    implementation(projects.innertube)
    implementation(projects.kugou)
    implementation(projects.lrclib)
    implementation(projects.piped)


//    coreLibraryDesugaring(libs.desugaring)
    coreLibraryDesugaring(libs.desugaring.nio)

    // Debug only
    debugImplementation(libs.ui.tooling.preview.android)
}
