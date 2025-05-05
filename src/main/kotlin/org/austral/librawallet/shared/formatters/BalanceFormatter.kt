package org.austral.librawallet.shared.formatters

import java.math.BigDecimal
import java.math.RoundingMode

fun centsToFormattedDouble(balanceInCents: Long): Double {
    return BigDecimal.valueOf(balanceInCents)
        .divide(BigDecimal(100))
        .setScale(2, RoundingMode.DOWN)
        .toDouble()
}