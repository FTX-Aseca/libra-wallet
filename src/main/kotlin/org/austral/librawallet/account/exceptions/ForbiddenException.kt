package org.austral.librawallet.account.exceptions

class ForbiddenException(message: String = "You do not have access to this account.") : RuntimeException(message)
