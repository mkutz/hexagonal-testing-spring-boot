package io.github.mkutz.hexagonaltesting.adaptersdriving.web

import io.github.mkutz.hexagonaltesting.TestcontainersConfiguration
import io.github.mkutz.hexagonaltesting.application.order.anOrder
import io.github.mkutz.hexagonaltesting.application.order.port.OrderRepository
import org.approvej.ApprovalBuilder.approve
import org.approvej.json.jackson3.JsonPrintFormat.json
import org.approvej.scrub.Scrubbers.uuids
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import

/**
 * Approval test on the outbound HTTP contract. It captures the whole serialized shape of the order
 * response and compares it against the approved snapshot, so the contract cannot change by
 * accident. UUIDs are scrubbed so the random IDs don't churn the snapshot.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class OrderResponseApprovalTest {

  @Autowired private lateinit var orderController: OrderController

  @Autowired private lateinit var orders: OrderRepository

  @Test
  fun orderResponseContract() {
    val order = anOrder()
    orders.save(order)

    val response = orderController.getOrder(order.id.value)

    approve(response).printedAs(json()).scrubbedOf(uuids()).byFile()
  }
}
