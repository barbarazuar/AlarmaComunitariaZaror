import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
    jacoco
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") // activa KSP en este modulo
}

jacoco { toolVersion = "0.8.14" }

// Lógica sin UI Compose, DTO, Room generado ni fábricas Android.
val clasesLogica = fileTree(layout.buildDirectory.dir(
    "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"
)) {
    include("**/viewmodel/**", "**/data/repository/AlarmaRepository*",
        "**/data/remote/ResultadoRemotoKt*", "**/util/**", "**/model/ClimaKt*",
        "**/model/RegistroErrores*", "**/model/AlertaErrores*")
    exclude("**/*\$Companion*")
}
val datosCobertura = layout.buildDirectory.file(
    "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
)

tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val coberturaDebug = tasks.register<JacocoReport>("coberturaDebug") {
    group = "verification"
    description = "Reporte HTML y XML de cobertura de la lógica Android."
    dependsOn("testDebugUnitTest")
    executionData(datosCobertura)
    classDirectories.setFrom(clasesLogica)
    sourceDirectories.setFrom(files("src/main/java"))
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
    doFirst { check(!clasesLogica.isEmpty) { "No se encontraron clases de lógica para medir" } }
}

tasks.register<JacocoCoverageVerification>("verificarCoberturaDebug") {
    group = "verification"
    description = "Exige al menos 80 % de líneas cubiertas en la lógica Android."
    dependsOn(coberturaDebug)
    executionData(datosCobertura)
    classDirectories.setFrom(clasesLogica)
    violationRules { rule { limit { counter = "LINE"; minimum = "0.80".toBigDecimal() } } }
}

android {
    namespace = "com.example.alarmacomunitariazaror"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.alarmacomunitariazaror"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navegacion entre pantallas
    implementation("androidx.navigation:navigation-compose:2.10.2")
    // viewModel() dentro de Composables
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    // Base de datos local Room
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    // Iconos extra de Material (Error, Visibility, History, etc.)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    // Ubicacion del dispositivo
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Mostrar fotos en pantalla
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Retrofit: cliente HTTP para los microservicios y la API externa
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Pruebas de repositorios: respuestas remotas simuladas y corrutinas
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}
