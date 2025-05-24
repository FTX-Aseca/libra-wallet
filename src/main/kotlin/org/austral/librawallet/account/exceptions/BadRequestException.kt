package org.austral.librawallet.account.exceptions

/**
 * Exception to denote a bad request, e.g., invalid DEBIN callback.
 */
class BadRequestException(message: String) : RuntimeException(message)
