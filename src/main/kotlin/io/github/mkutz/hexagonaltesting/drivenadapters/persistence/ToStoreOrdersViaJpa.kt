package io.github.mkutz.hexagonaltesting.drivenadapters.persistence

import io.github.mkutz.hexagonaltesting.application.order.CustomerId
import io.github.mkutz.hexagonaltesting.application.order.Money
import io.github.mkutz.hexagonaltesting.application.order.Order
import io.github.mkutz.hexagonaltesting.application.order.OrderId
import io.github.mkutz.hexagonaltesting.application.order.port.ToStoreOrders
import org.springframework.stereotype.Repository

/** Driven adapter: persists [Order]s through Spring Data JPA. */
@Repository
class ToStoreOrdersViaJpa(private val repository: OrderRepository) : ToStoreOrders {

  override fun save(order: Order) {
    repository.save(order.toEntity())
  }

  override fun findById(id: OrderId): Order? =
    repository.findById(id.value).map(OrderEntity::toDomain).orElse(null)
}

private fun Order.toEntity() = OrderEntity(id.value, customer.value, amount.minorUnits)

private fun OrderEntity.toDomain() =
  Order(OrderId(id), CustomerId(customerId), Money.ofMinorUnits(amountMinor))
