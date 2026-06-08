package io.github.mkutz.hexagonaltesting.application.order

data class PlaceOrderCommand(val customer: CustomerId, val amount: Money)
