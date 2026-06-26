package io.github.mkutz.hexagonaltesting

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Service

/**
 * Guards the ports-and-adapters boundaries the whole testing strategy relies on. The rules scan the
 * compiled production code (the `main` source set only) and fail the build the moment a dependency
 * crosses a seam it shouldn't.
 *
 * Note on "via ports": adapters legitimately use the core's domain, command and result types –
 * those *are* the contract a port's signature exposes. What "via ports" forbids is reaching past
 * the port interface to call a use-case implementation directly, which is what the last rule pins
 * down.
 */
class HexagonalArchitectureTest {

  // Scan production (`main`) classes only. Test fixtures deliberately bridge layers (e.g. builders
  // that return adapter DTOs), so including them would trip these rules. We exclude the unit-test
  // classes and the test-fixtures artifact by source location, covering both the Gradle jar names
  // (`-test-fixtures.jar`) and the raw IDE output dirs (`testFixtures`).
  private val productionClasses =
    ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
      .withImportOption { !it.contains("test-fixtures") && !it.contains("testFixtures") }
      .importPackages("io.github.mkutz.hexagonaltesting")

  @Test
  fun `the core depends only on itself, never on an adapter or a web or persistence framework`() {
    noClasses()
      .that()
      .resideInAPackage("..application..")
      .should()
      .dependOnClassesThat()
      .resideInAnyPackage(
        "..adaptersdriving..",
        "..drivenadapters..",
        "org.springframework.web..",
        "org.springframework.http..",
        "org.springframework.data..",
        "jakarta.persistence..",
      )
      .because(
        "the application core must stay framework-agnostic and reach the outside world only " +
          "through the ports it owns"
      )
      .check(productionClasses)
  }

  @Test
  fun `driving adapters never depend on driven adapters`() {
    noClasses()
      .that()
      .resideInAPackage("..adaptersdriving..")
      .should()
      .dependOnClassesThat()
      .resideInAPackage("..drivenadapters..")
      .because("the two adapter families must meet only inside the core, never directly")
      .check(productionClasses)
  }

  @Test
  fun `driven adapters never depend on driving adapters`() {
    noClasses()
      .that()
      .resideInAPackage("..drivenadapters..")
      .should()
      .dependOnClassesThat()
      .resideInAPackage("..adaptersdriving..")
      .because("the two adapter families must meet only inside the core, never directly")
      .check(productionClasses)
  }

  @Test
  fun `adapters reach the core through ports, never through a use-case implementation`() {
    classes()
      .that()
      .areAnnotatedWith(Service::class.java)
      .should()
      .onlyHaveDependentClassesThat()
      .resideInAPackage("..application..")
      .because("driving adapters must invoke the core through its input ports, not its services")
      .check(productionClasses)
  }
}
