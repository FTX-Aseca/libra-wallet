package org.austral.librawallet.account.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AccountRestExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<Map<String, String>> {
        val body = mapOf("error" to (ex.message ?: "Not found"))
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }
}