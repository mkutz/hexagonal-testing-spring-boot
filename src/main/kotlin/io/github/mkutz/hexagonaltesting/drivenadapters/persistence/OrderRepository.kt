package io.github.mkutz.hexagonaltesting.drivenadapters.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderEntity, UUID>
