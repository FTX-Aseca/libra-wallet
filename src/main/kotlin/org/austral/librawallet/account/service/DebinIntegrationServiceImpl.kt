package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Implementation of DebinIntegrationService that calls the external fake API.
 */
@Service
class DebinIntegrationServiceImpl : DebinIntegrationService {
    private val restTemplate = RestTemplate()
    private val baseUrl = "http://localhost:5000" // TODO: move this as an env var

    override fun performDebin(
        identifierType: IdentifierType,
        fromIdentifier: String,
        amountInCents: Long,
    ): Boolean {
        val payload = mapOf(
            "identifier_type" to identifierType.name.lowercase(),
            "identifier" to fromIdentifier,
            "amount" to amountInCents,
        )
        return try {
            val response: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(
                "$baseUrl/api/debin",
                HttpEntity(payload),
                Map::class.java,
            )
            response.statusCode == HttpStatus.OK
        } catch (_: Exception) {
            false
        }
    }
}
