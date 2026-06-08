package io.github.mkutz.hexagonaltesting.application.order

data class Order(val id: OrderId, val customer: CustomerId, val amount: Money)
