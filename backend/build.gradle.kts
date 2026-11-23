// Se declaran los plugins de Spring una sola vez; cada microservicio los aplica en su build.gradle.kts
plugins {
    id("org.springframework.boot") version "3.5.7" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}
