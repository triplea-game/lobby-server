plugins {
    id("java")
    id("io.freefair.lombok") version "9.5.0"
    id("com.diffplug.spotless") version "8.4.0"
    id("io.quarkus") version "3.35.2"
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
        events("skipped", "failed")
    }
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
    val outputByTest = mutableMapOf<String, StringBuilder>()

    addTestOutputListener { descriptor, event ->
        val key = "${descriptor.className}.${descriptor.name}"
        outputByTest.getOrPut(key) { StringBuilder() }.append(event.message)
    }

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}

        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            val key = "${testDescriptor.className}.${testDescriptor.name}"
            val output = outputByTest.remove(key)
            if (result.resultType == TestResult.ResultType.FAILURE && !output.isNullOrEmpty()) {
                println("\n-- Output for ${testDescriptor.displayName} --\n$output")
            }
        }
    })
}

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
    }
}

val quarkusPlatformVersion = "3.35.2"
val tripleaVersion = "2.7.15498"

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
    implementation("org.jdbi:jdbi3-core:3.53.0")
    implementation("org.jdbi:jdbi3-sqlobject:3.53.0")

    // Utility libraries (no longer pulled in transitively post-DropWizard removal)
    implementation("com.google.guava:guava")
    implementation("com.google.code.findbugs:jsr305")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Non-DropWizard dependencies retained from before
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("com.sun.mail:jakarta.mail:2.0.2")
    implementation("triplea:lobby-client-data:$tripleaVersion")

    testImplementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-flyway")              // run DB migrations on startup
    implementation("org.flywaydb:flyway-database-postgresql") // Flyway PostgreSQL support

    testImplementation("io.quarkus:quarkus-junit5")         // @QuarkusTest + Dev Services

    testImplementation("org.java-websocket:Java-WebSocket:1.6.0")

    testImplementation("com.github.database-rider:rider-junit5:1.43.0")
    testImplementation("com.github.npathai:hamcrest-optional:2.0.0")
    testImplementation("org.assertj:assertj-core:3.27.7")

    testImplementation("org.awaitility:awaitility:4.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.13.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")

    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testImplementation("org.wiremock:wiremock:3.7.0")
    testImplementation("ru.lanwen.wiremock:wiremock-junit5:1.3.1")
    testImplementation("uk.co.datumedge:hamcrest-json:0.3")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.0")
}
