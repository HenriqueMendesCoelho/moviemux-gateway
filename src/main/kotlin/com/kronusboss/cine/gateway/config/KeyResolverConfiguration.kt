package com.kronusboss.cine.gateway.config

import com.kronusboss.cine.gateway.util.JWTUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Mono

@Configuration
class KeyResolverConfiguration {

    @Autowired
    private lateinit var jwtUtil: JWTUtil

    @Bean
    fun userKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            val token = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.substring(7)
            val claims = token?.let { jwtUtil.getClaims(it) }
            if (claims != null) {
                return@KeyResolver Mono.just(claims.subject)
            }


            val forwardedFor = exchange.request.headers["x-forwarded-for"]?.firstOrNull()
            val remoteAddress = exchange.request.remoteAddress?.address?.hostAddress
            return@KeyResolver Mono.just(forwardedFor?.takeIf { it.isNotBlank() }
                ?: remoteAddress ?: "unknown")
        }
    }
}
