package com.kronusboss.cine.gateway.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class KeyResolverConfiguration {
    @Bean
    fun notAuthenticatedKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            val forwardedFor = exchange.request.headers["X-Forwarded-for"]?.firstOrNull()
            val remoteAddress = exchange.request.remoteAddress?.address?.hostAddress

            Mono.just(forwardedFor?.takeIf { it.isNotBlank() } ?: remoteAddress ?: "unknown")
        }
    }
}
