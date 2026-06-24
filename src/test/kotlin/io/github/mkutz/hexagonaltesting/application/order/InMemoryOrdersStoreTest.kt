package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.ToStoreOrders

/** Runs the shared [ToStoreOrdersContract] against the in-memory fake (a plain unit test). */
class InMemoryOrdersStoreTest : ToStoreOrdersContract() {

  private val store = InMemoryOrdersStore()

  override fun store(): ToStoreOrders = store
}
