package io.github.mkutz.hexagonaltesting.adaptersdriving.web

import java.util.UUID

data class OrderResponse(val orderId: UUID, val customerId: UUID, val amountMinor: Long)
