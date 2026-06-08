@file:Suppress("UnstableApiUsage")

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.plugin.spring)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
  alias(libs.plugins.kotlin.plugin.jpa)
  alias(libs.plugins.spotless)
  `jvm-test-suite`
  `java-test-fixtures`
}

group = "io.github.mkutz"

version = "0.0.1-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }

// Kotlin 2.2.21 tops out at JVM target 24, so target 24 bytecode for both compilers
// (the JDK 25 toolchain still runs the build) to keep Java/Kotlin targets consistent.
tasks.withType<JavaCompile>().configureEach { options.release = 24 }

repositories { mavenCentral() }

dependencies {
  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.spring.boot.starter.restclient)
  implementation(libs.spring.boot.starter.validation)
  implementation(libs.spring.boot.starter.webmvc)
  implementation(libs.kotlin.reflect)
  implementation(libs.jackson.module.kotlin)
  runtimeOnly(libs.postgresql)

  // Shared test code (fakes, contract suites, data builders) lives in src/testFixtures.
  testFixturesImplementation(libs.kotlin.test.junit5)
  testFixturesImplementation(libs.assertj.core)
}

testing {
  suites {
    // Unit suite: fast, no Spring context, no infrastructure.
    val test by
      getting(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(testFixtures(project()))
          implementation(libs.assertj.core)
        }
      }

    // Adapter/integration suite: real infrastructure via Testcontainers + WireMock.
    val integrationTest by
      registering(JvmTestSuite::class) {
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

kotlin {
  compilerOptions {
    jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24
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
