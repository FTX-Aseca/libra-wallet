package org.austral.librawallet.auth.config

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.time.Instant
import java.util.*

class WithMockJwtSecurityContextFactory : WithSecurityContextFactory<WithMockJwt> {

    override fun createSecurityContext(withMockJwt: WithMockJwt): org.springframework.security.core.context.SecurityContext {
        val context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext()

        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf(
                "sub" to withMockJwt.subject,
                "scope" to "read write",
            ),
        )

        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = JwtAuthenticationToken(jwt, authorities)
        context.authentication = authentication

        return context
    }
}
