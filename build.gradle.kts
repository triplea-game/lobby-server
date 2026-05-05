plugins {
    id("java")
    id("io.freefair.lombok") version "8.14.2"
    id("com.diffplug.spotless") version "7.2.1"
    id("io.quarkus") version "3.34.6"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/triplea-game/triplea")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("triplea_github_username") as String?
            password = System.getenv("GH_TOKEN") ?: project.findProperty("triplea_github_access_token") as String?
        }
    }
}


/* "testInteg" runs tests that require a database or a server to be running */
val testInteg: SourceSet = sourceSets.create("testInteg") {
    java {
        java.srcDir("src/testInteg/java")
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += output + compileClasspath
    }
}

configurations[testInteg.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[testInteg.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val testIntegTask = tasks.register<Test>("testInteg") {
    group = "verification"
    useJUnitPlatform()
    testClassesDirs = sourceSets["testInteg"].output.classesDirs
    classpath = sourceSets["testInteg"].runtimeClasspath
    // Required for Quarkus @QuarkusTest to use the JBoss log manager
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.check {
    dependsOn(testIntegTask)
}

tasks.clean {
    // Clean Quarkus build artifacts
    delete("build/quarkus-app")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("standardOut", "standardError", "skipped", "failed")
    }
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
    }
}

val quarkusPlatformVersion = "3.34.6"
val tripleaVersion = "2.7.15281"

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusPlatformVersion"))

    // Quarkus extensions
    implementation("io.quarkus:quarkus-resteasy-jackson")   // JAX-RS (Classic) + Jackson
    implementation("io.quarkus:quarkus-agroal")             // JDBC connection pool
    implementation("io.quarkus:quarkus-jdbc-postgresql")    // PostgreSQL + Dev Services
    implementation("io.quarkus:quarkus-security")           // @RolesAllowed, SecurityContext
    implementation("io.quarkus:quarkus-websockets")         // JSR-356 WebSocket support
    implementation("io.quarkus:quarkus-smallrye-health")    // health checks

    // JDBI — framework-agnostic, wires against any DataSource
    implementation("org.jdbi:jdbi3-core:3.49.5")
    implementation("org.jdbi:jdbi3-sqlobject:3.49.5")

    // Utility libraries (no longer pulled in transitively post-DropWizard removal)
    implementation("com.google.guava:guava")
    implementation("com.google.code.findbugs:jsr305")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Non-DropWizard dependencies retained from before
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.sun.mail:jakarta.mail:2.0.2")
    // org.java-websocket is only used by integration-test WebSocket clients; not needed at runtime
    testImplementation("org.java-websocket:Java-WebSocket:1.6.0")
    implementation("triplea:domain-data:$tripleaVersion")
    implementation("triplea:feign-common:$tripleaVersion")
    implementation("triplea:java-extras:$tripleaVersion")
    implementation("triplea:lobby-client:$tripleaVersion")
    implementation("triplea:websocket-client:$tripleaVersion")

    // feign-core and feign-gson are still required at runtime because triplea:feign-common and
    // triplea:lobby-client were compiled against feign and load feign classes at runtime.
    runtimeOnly("io.github.openfeign:feign-core:13.6")
    runtimeOnly("io.github.openfeign:feign-gson:13.6")

    testImplementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-flyway")              // run DB migrations on startup
    implementation("org.flywaydb:flyway-database-postgresql") // Flyway PostgreSQL support

    testImplementation("io.quarkus:quarkus-junit5")         // @QuarkusTest + Dev Services

    // feign-core on the test compile classpath allows integration tests to assert on FeignException
    testImplementation("io.github.openfeign:feign-core:13.6")

    testImplementation("com.github.database-rider:rider-junit5:1.43.0")
    testImplementation("com.github.npathai:hamcrest-optional:2.0.0")
    testImplementation("com.sun.mail:jakarta.mail:2.0.2")
    testImplementation("org.awaitility:awaitility:4.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.13.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")

    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
    testImplementation("org.wiremock:wiremock:3.7.0")
    testImplementation("ru.lanwen.wiremock:wiremock-junit5:1.3.1")
    testImplementation("uk.co.datumedge:hamcrest-json:0.3")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")
}
