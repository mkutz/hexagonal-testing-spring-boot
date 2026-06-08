package io.github.mkutz.hexagonaltesting

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import

/** Wiring/configuration smoke test: the assembled application context starts. */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class HexagonaltestingApplicationTests {

  @Test fun contextLoads() {}
}
