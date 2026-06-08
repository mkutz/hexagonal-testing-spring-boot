package io.github.mkutz.hexagonaltesting.drivenadapters.persistence

import io.github.mkutz.hexagonaltesting.application.order.CustomerId
import io.github.mkutz.hexagonaltesting.application.order.Money
import io.github.mkutz.hexagonaltesting.application.order.Order
import io.github.mkutz.hexagonaltesting.application.order.OrderId
import io.github.mkutz.hexagonaltesting.application.order.port.OrderRepository
import org.springframework.stereotype.Repository

/** Driven adapter: persists [Order]s through Spring Data JPA. */
@Repository
class JpaOrderRepository(private val orders: SpringDataOrderRepository) : OrderRepository {

  override fun save(order: Order) {
    orders.save(order.toEntity())
  }

  override fun findById(id: OrderId): Order? =
    orders.findById(id.value).map(OrderEntity::toDomain).orElse(null)
}

private fun Order.toEntity() = OrderEntity(id.value, customer.value, amount.minorUnits)

private fun OrderEntity.toDomain() =
  Order(OrderId(id), CustomerId(customerId), Money.ofMinorUnits(amountMinor))
