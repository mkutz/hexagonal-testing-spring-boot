package io.github.mkutz.hexagonaltesting.application.order

import io.github.mkutz.hexagonaltesting.application.order.port.OrderRepository

/** Runs the shared [OrderRepositoryContract] against the in-memory fake (a plain unit test). */
class InMemoryOrderRepositoryTest : OrderRepositoryContract() {

  private val repository = InMemoryOrderRepository()

  override fun repository(): OrderRepository = repository
}
