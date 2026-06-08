package io.github.mkutz.hexagonaltesting.application.order

import java.util.UUID

@JvmInline
value class OrderId(val value: UUID) {
  companion object {
    fun random() = OrderId(UUID.randomUUID())
  }
}
