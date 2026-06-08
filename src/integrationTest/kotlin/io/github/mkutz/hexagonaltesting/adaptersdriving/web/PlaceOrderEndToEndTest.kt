package io.github.mkutz.hexagonaltesting.adaptersdriving.web

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import io.github.mkutz.hexagonaltesting.TestcontainersConfiguration
import io.github.mkutz.hexagonaltesting.application.order.OrderId
import io.github.mkutz.hexagonaltesting.application.order.port.OrderRepository
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import

/**
 * Exercises the whole assembled application end to end through a real HTTP client, verifying the
 * outcome via the injected driven port. This is the only layer that proves the wiring works; it
 * does not re-test the business rules.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class PlaceOrderEndToEndTest {

  @Autowired private lateinit var orders: OrderRepository

  @Autowired private lateinit var paymentServer: WireMockServer

  @LocalServerPort private var port: Int = 0

  @BeforeEach
  fun stubPaymentService() {
    paymentServer.resetAll()
    paymentServer.stubFor(
      get(urlPathMatching("/customers/.*/credit-limit"))
        .willReturn(okJson("""{"limitMinor": 100000000}"""))
    )
    paymentServer.stubFor(post(urlPathMatching("/customers/.*/charges")).willReturn(ok()))
  }

  @Test
  fun `posting an order persists it`() {
    val customerId = UUID.randomUUID() // random — no collision with other tests
    val request =
      HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:$port/orders"))
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofString("""{ "customerId": "$customerId", "amount": "80.00" }"""))
        .build()

    val response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString())

    assertThat(response.statusCode()).isEqualTo(201)
    val orderId = Regex(""""orderId"\s*:\s*"([^"]+)"""").find(response.body())!!.groupValues[1]
    // Assert THIS order exists — never "assert exactly one order exists".
    assertThat(orders.findById(OrderId(UUID.fromString(orderId)))).isNotNull()
  }
}
