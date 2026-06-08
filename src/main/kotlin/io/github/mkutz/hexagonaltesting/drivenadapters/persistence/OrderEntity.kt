package io.github.mkutz.hexagonaltesting.drivenadapters.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "orders")
class OrderEntity(
  @Id val id: UUID,
  @Column(name = "customer_id", nullable = false) val customerId: UUID,
  @Column(name = "amount_minor", nullable = false) val amountMinor: Long,
)
