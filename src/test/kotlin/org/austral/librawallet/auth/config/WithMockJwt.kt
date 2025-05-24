package org.austral.librawallet.auth.config

import org.springframework.security.test.context.support.WithSecurityContext
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.Retention

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory::class)
annotation class WithMockJwt(
    val subject: String = "1",
)
