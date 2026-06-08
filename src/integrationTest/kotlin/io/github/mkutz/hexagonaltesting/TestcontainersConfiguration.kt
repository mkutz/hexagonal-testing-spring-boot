package io.github.mkutz.hexagonaltesting

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Shared infrastructure for the integration suite: a real Postgres via Testcontainers and a
 * WireMock stand-in for the external payment service. Every integration test imports it with the
 * same `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@Import(TestcontainersConfiguration)`, so
 * the whole suite reuses a single application context.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  fun postgresContainer(): PostgreSQLContainer =
    PostgreSQLContainer(DockerImageName.parse("postgres:16"))

  @Bean(destroyMethod = "stop")
  fun paymentServer(): WireMockServer =
    WireMockServer(wireMockConfig().dynamicPort()).apply { start() }

  @Bean
  fun paymentProperties(paymentServer: WireMockServer) = DynamicPropertyRegistrar { registry ->
    registry.add("payment.base-url") { paymentServer.baseUrl() }
  }
}
