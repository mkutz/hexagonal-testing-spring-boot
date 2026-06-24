package io.github.mkutz.hexagonaltesting.drivenadapters.payment

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.github.mkutz.hexagonaltesting.TestcontainersConfiguration
import io.github.mkutz.hexagonaltesting.application.order.CustomerId
import io.github.mkutz.hexagonaltesting.application.order.Money
import io.github.mkutz.hexagonaltesting.application.order.port.ToHandlePayments
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import

/**
 * Drives the real `PaymentHandler` adapter against a WireMock stand-in for the third-party payment
 * service — the integration risk only real HTTP can reveal: URL, serialization, response mapping.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class PaymentHandlerIntegrationTest {

  @Autowired private lateinit var paymentHandler: ToHandlePayments

  @Autowired private lateinit var paymentServer: WireMockServer

  @BeforeEach
  fun resetStubs() {
    paymentServer.resetAll()
  }

  @Test
  fun `reads the credit limit from the payment service`() {
    val customer = CustomerId.random()
    paymentServer.stubFor(
      get(urlPathEqualTo("/customers/${customer.value}/credit-limit"))
        .willReturn(okJson("""{"limitMinor": 50000}"""))
    )

    assertThat(paymentHandler.creditLimit(customer)).isEqualTo(Money.ofMinorUnits(50_000))
  }

  @Test
  fun `posts the charged amount in minor units`() {
    val customer = CustomerId.random()
    paymentServer.stubFor(
      post(urlPathEqualTo("/customers/${customer.value}/charges")).willReturn(ok())
    )

    paymentHandler.charge(customer, Money.euros(80))

    paymentServer.verify(
      postRequestedFor(urlPathEqualTo("/customers/${customer.value}/charges"))
        .withRequestBody(matchingJsonPath("$.amountMinor", equalTo("8000")))
    )
  }
}
