package io.github.mkutz.hexagonaltesting.drivenadapters.payment

import io.github.mkutz.hexagonaltesting.application.order.CustomerId
import io.github.mkutz.hexagonaltesting.application.order.Money
import io.github.mkutz.hexagonaltesting.application.order.port.ToHandlePayments
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

/** Driven adapter: talks to the external payment service over HTTP. */
@Component
class ToHandlePaymentsViaRest(private val paymentRestClient: RestClient) : ToHandlePayments {

  override fun creditLimit(customer: CustomerId): Money {
    val response =
      paymentRestClient
        .get()
        .uri("/customers/{id}/credit-limit", customer.value)
        .retrieve()
        .body<CreditLimitResponse>()
    return Money.ofMinorUnits(response!!.limitMinor)
  }

  override fun charge(customer: CustomerId, amount: Money) {
    paymentRestClient
      .post()
      .uri("/customers/{id}/charges", customer.value)
      .body(ChargeRequest(amount.minorUnits))
      .retrieve()
      .toBodilessEntity()
  }
}

data class CreditLimitResponse(val limitMinor: Long)

data class ChargeRequest(val amountMinor: Long)
