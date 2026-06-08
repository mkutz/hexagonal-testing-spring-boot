package io.github.mkutz.hexagonaltesting.adaptersdriving.web

import io.github.mkutz.hexagonaltesting.application.order.CustomerId
import io.github.mkutz.hexagonaltesting.application.order.Money
import io.github.mkutz.hexagonaltesting.application.order.Order
import io.github.mkutz.hexagonaltesting.application.order.OrderId
import io.github.mkutz.hexagonaltesting.application.order.OrderPlaced
import io.github.mkutz.hexagonaltesting.application.order.OrderRejected
import io.github.mkutz.hexagonaltesting.application.order.PlaceOrder
import io.github.mkutz.hexagonaltesting.application.order.PlaceOrderCommand
import io.github.mkutz.hexagonaltesting.application.order.port.OrderRepository
import java.util.UUID
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/** Driving adapter: the inbound REST endpoint for orders. */
@RestController
@RequestMapping("/orders")
class OrderController(private val placeOrder: PlaceOrder, private val orders: OrderRepository) {

  @PostMapping
  fun place(@RequestBody request: PlaceOrderRequest): ResponseEntity<OrderResponse> =
    when (
      val result =
        placeOrder.handle(
          PlaceOrderCommand(CustomerId(request.customerId), Money.ofEuros(request.amount))
        )
    ) {
      is OrderPlaced ->
        ResponseEntity.status(CREATED).body(orders.findById(result.orderId)!!.toResponse())
      is OrderRejected -> ResponseEntity.unprocessableContent().build()
    }

  @GetMapping("/{id}")
  fun getOrder(@PathVariable id: UUID): OrderResponse =
    orders.findById(OrderId(id))?.toResponse()
      ?: throw ResponseStatusException(NOT_FOUND, "No order $id")
}

private fun Order.toResponse() = OrderResponse(id.value, customer.value, amount.minorUnits)
