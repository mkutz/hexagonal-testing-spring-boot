package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.ToStoreOrders

/** Runs the shared [ToStoreOrdersContract] against the in-memory fake (a plain unit test). */
class ToStoreOrdersInMemoryTest : ToStoreOrdersContract() {

  private val store = ToStoreOrdersInMemory()

  override fun store(): ToStoreOrders = store
}
