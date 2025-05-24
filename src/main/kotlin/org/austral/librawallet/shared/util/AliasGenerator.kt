package org.austral.librawallet.shared.util

import java.util.*

private const val MAX_RANDOM_NUMBER = 1000

private const val N_DIGITS = 3

private const val FILLER_CHAR = '0'

object AliasGenerator {
    private val random = Random()
    private val adjectives = listOf("sunny", "happy", "cool", "bright", "fast")
    private val nouns = listOf("river", "mountain", "star", "sky", "tree")

    fun generate(): String {
        val adjective = adjectives[random.nextInt(adjectives.size)]
        val noun = nouns[random.nextInt(nouns.size)]
        val number = random.nextInt(MAX_RANDOM_NUMBER).toString().padStart(N_DIGITS, FILLER_CHAR)
        return "$adjective.$noun.$number"
    }
}
