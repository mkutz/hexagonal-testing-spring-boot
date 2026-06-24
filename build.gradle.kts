@file:Suppress("UnstableApiUsage")

plugins {
  jacoco
  `java-test-fixtures`
  `jvm-test-suite`
  alias(libs.plugins.approvej)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.plugin.jpa)
  alias(libs.plugins.kotlin.plugin.spring)
  alias(libs.plugins.sonarqube)
  alias(libs.plugins.spotless)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
}

group = "io.github.mkutz"

version = "0.0.1-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }

repositories { mavenCentral() }

dependencies {
  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.spring.boot.starter.restclient)
  implementation(libs.spring.boot.starter.validation)
  implementation(libs.spring.boot.starter.webmvc)
  implementation(libs.kotlin.reflect)
  implementation(libs.jackson.module.kotlin)
  runtimeOnly(libs.postgresql)
  testFixturesImplementation(libs.kotlin.test.junit5)
  testFixturesImplementation(libs.assertj.core)
}

testing {
  suites {
    // Unit suite: fast, no Spring context, no infrastructure.
    val test =
      getByName<JvmTestSuite>("test") {
        useJUnitJupiter()
        dependencies {
          implementation(testFixtures(project()))
          implementation(libs.assertj.core)
        }
      }

    // Adapter/integration suite: real infrastructure via Testcontainers + WireMock.
    register<JvmTestSuite>("integrationTest") {
      useJUnitJupiter()
      dependencies {
        implementation(project())
        implementation(testFixtures(project()))
        implementation(libs.spring.boot.starter.data.jpa.test)
        implementation(libs.spring.boot.starter.restclient.test)
        implementation(libs.spring.boot.starter.validation.test)
        implementation(libs.spring.boot.starter.webmvc.test)
        implementation(libs.spring.boot.testcontainers)
        implementation(libs.testcontainers.junit.jupiter)
        implementation(libs.testcontainers.postgresql)
        implementation(libs.assertj.core)
        implementation(libs.approvej.core)
        implementation(libs.approvej.json.jackson3)
        implementation(libs.wiremock)
      }
      targets { all { testTask.configure { shouldRunAfter(test) } } }
    }
  }
}

tasks.named("check") { dependsOn(testing.suites.named("integrationTest")) }

// One report covering BOTH suites. The dedicated `jacoco-report-aggregation` plugin is deliberately
// not used: it keys reports by test-suite type (yielding a separate `testCodeCoverageReport` and
// `integrationTestCodeCoverageReport`), which is built for aggregating across projects, not for
// merging suites within one. Each per-suite report is an incomplete picture — e.g. the integration
// suite alone misses the credit-limit rejection branch that only the unit tests exercise. Merging
// the two `.exec` files is the only honest view of "what the whole pyramid covers", and it's what
// Sonar Cloud consumes.
val testTasks = testing.suites.withType<JvmTestSuite>().flatMap { it.targets }.map { it.testTask }

val jacocoMergedReport =
  tasks.register<JacocoReport>("jacocoMergedReport") {
    group = "verification"
    description = "Merged coverage across the unit and integration test suites."
    dependsOn(testTasks)
    // Read each suite test task's own JaCoCo exec destination — no hardcoded paths, so this keeps
    // working if a suite is renamed or added.
    executionData(
      files(testTasks.map { it.map { task -> task.the<JacocoTaskExtension>().destinationFile } })
    )
    sourceSets(sourceSets.main.get())
    reports { xml.required = true }
  }

tasks.named("check") { dependsOn(jacocoMergedReport) }

sonar {
  properties {
    property("sonar.projectKey", "mkutz_hexagonal-testing-spring-boot")
    property("sonar.organization", "mkutz")
    property("sonar.host.url", "https://sonarcloud.io")
    // Treat the integration suite as test sources too, so Sonar attributes its execution correctly.
    property("sonar.tests", "src/test/kotlin,src/integrationTest/kotlin")
    property(
      "sonar.coverage.jacoco.xmlReportPaths",
      layout.buildDirectory
        .file("reports/jacoco/jacocoMergedReport/jacocoMergedReport.xml")
        .get()
        .asFile
        .path,
    )
  }
}

// Sonar needs the merged XML to exist before it analyses.
tasks.named("sonar") { dependsOn(jacocoMergedReport) }

kotlin {
  compilerOptions {
    jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
    freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
  }
}

allOpen {
  annotation("jakarta.persistence.Entity")
  annotation("jakarta.persistence.MappedSuperclass")
  annotation("jakarta.persistence.Embeddable")
}

spotless {
  kotlin { ktfmt().googleStyle() }
  kotlinGradle { ktfmt().googleStyle() }
  toml {
    target("gradle/libs.versions.toml")
    versionCatalog()
  }
  flexmark {
    target("**/*.md")
    flexmark()
  }
}
