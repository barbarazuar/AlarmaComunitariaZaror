// Se declaran los plugins de Spring una sola vez; cada microservicio los aplica en su build.gradle.kts
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("org.springframework.boot") version "3.5.7" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

subprojects {
    apply(plugin = "jacoco")
    extensions.configure<JacocoPluginExtension> { toolVersion = "0.8.14" }
    plugins.withId("java") {
        val logica = fileTree(layout.buildDirectory.dir("classes/java/main")) {
            include("**/service/**")
        }
        tasks.named<JacocoReport>("jacocoTestReport") {
            dependsOn("test")
            classDirectories.setFrom(logica)
            reports { html.required.set(true); xml.required.set(true) }
        }
        tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
            dependsOn("jacocoTestReport")
            classDirectories.setFrom(logica)
            violationRules { rule { limit { counter = "LINE"; minimum = "0.80".toBigDecimal() } } }
        }
    }
}
