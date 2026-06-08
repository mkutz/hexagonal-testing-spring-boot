package io.github.mkutz.hexagonaltesting.application.order

import java.util.UUID

@JvmInline
value class CustomerId(val value: UUID) {
  companion object {
    fun random() = CustomerId(UUID.randomUUID())
  }
}
