package io.github.mkutz.hexagonaltesting.adaptersdriving.web

import java.math.BigDecimal
import java.util.UUID

data class PlaceOrderRequest(val customerId: UUID, val amount: BigDecimal)
