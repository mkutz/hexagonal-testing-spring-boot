package io.github.mkutz.hexagonaltesting.drivenadapters.payment

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class PaymentGatewayConfig {

  @Bean
  fun paymentRestClient(@Value("\${payment.base-url}") baseUrl: String): RestClient =
    RestClient.builder().baseUrl(baseUrl).build()
}
