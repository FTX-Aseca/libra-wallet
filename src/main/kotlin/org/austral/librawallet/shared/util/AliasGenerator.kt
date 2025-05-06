package org.austral.librawallet.shared.util

import java.util.Random

object AliasGenerator {
    private val random = Random()
    private val adjectives = listOf("sunny", "happy", "cool", "bright", "fast")
    private val nouns = listOf("river", "mountain", "star", "sky", "tree")

    fun generate(): String {
        val adjective = adjectives[random.nextInt(adjectives.size)]
        val noun = nouns[random.nextInt(nouns.size)]
        val number = random.nextInt(1000).toString().padStart(3, '0')
        return "$adjective.$noun.$number"
    }
}