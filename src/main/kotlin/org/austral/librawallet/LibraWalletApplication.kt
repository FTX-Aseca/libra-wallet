package org.austral.librawallet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LibraWalletApplication

fun main(args: Array<String>) {
	runApplication<LibraWalletApplication>(*args)
}
