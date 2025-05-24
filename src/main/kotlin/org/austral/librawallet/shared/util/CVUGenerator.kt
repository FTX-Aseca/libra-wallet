package org.austral.librawallet.shared.util

import java.util.*

object CVUGenerator {
    private val random = Random()

    fun generate(): String {
        return (1..22)
            .joinToString("") { random.nextInt(10).toString() }
    }
}
