package io.github.mkutz.hexagonaltesting.application.order

import java.math.BigDecimal

/** A monetary amount, stored as minor units (cents) to avoid floating-point drift. */
@JvmInline
value class Money private constructor(val minorUnits: Long) : Comparable<Money> {

  override fun compareTo(other: Money) = minorUnits.compareTo(other.minorUnits)

  companion object {
    /** e.g. `Money.euros(80)` → 8000 minor units. */
    fun euros(major: Long) = Money(major * 100)

    fun ofMinorUnits(minorUnits: Long) = Money(minorUnits)

    /** Parses a major-unit decimal like `80.00` into minor units (8000). */
    fun ofEuros(amount: BigDecimal) = Money(amount.movePointRight(2).longValueExact())
  }
}
