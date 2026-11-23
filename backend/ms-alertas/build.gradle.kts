plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "cl.zaror"
version = "1.0.0"

java {
    // Java 17 para que corra en cualquier PC con JDK 17 o superior (Android Studio trae JDK 21)
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")        // API REST (controladores, JSON)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")   // persistencia con JPA/Hibernate
    implementation("org.springframework.boot:spring-boot-starter-validation") // @NotBlank, @Email, etc.
    runtimeOnly("com.h2database:h2")                                          // base de datos H2 guardada en archivo

    testImplementation("org.springframework.boot:spring-boot-starter-test")   // JUnit 5 + Mockito + MockMvc
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
