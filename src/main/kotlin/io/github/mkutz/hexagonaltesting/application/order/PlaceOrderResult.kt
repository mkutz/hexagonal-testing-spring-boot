package io.github.mkutz.hexagonaltesting.application.order

sealed interface PlaceOrderResult

data class OrderPlaced(val orderId: OrderId) : PlaceOrderResult

data class OrderRejected(val reason: String) : PlaceOrderResult
