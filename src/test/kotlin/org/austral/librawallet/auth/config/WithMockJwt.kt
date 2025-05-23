package org.austral.librawallet.auth.config

import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory::class)
annotation class WithMockJwt(
    val subject: String = "1"
) 