package com.kronusboss.cine.gateway.gatewayfilters

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

@Component
class JwtAuthenticationFilter :
    AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config>(Config::class.java) {

    class Config

    @Value("\${jwt.secret}")
    lateinit var secret: String

    override fun apply(config: Config?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request: ServerHttpRequest = exchange.request

            if (!request.headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                return@GatewayFilter unauthorized(exchange)
            }

            val authHeader =
                request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return@GatewayFilter unauthorized(exchange)
            if (!authHeader.startsWith("Bearer ")) {
                unauthorized(exchange)
            }

            val token = authHeader.substring(7)

            try {
                val claims: Claims = getClaims(token)
                    ?: return@GatewayFilter forbid(exchange)

                exchange.attributes["claims"] = claims
                chain.filter(exchange)
            } catch (e: Exception) {
                forbid(exchange)
            }
        }
    }

    private fun unauthorized(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        return exchange.response.setComplete()
    }

    private fun forbid(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.FORBIDDEN
        return exchange.response.setComplete()
    }

    private fun getClaims(token: String): Claims? {
        return try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).payload
        } catch (e: java.lang.Exception) {
            println("Error parsing token: ${e.message}")
            null
        }
    }

    private fun getSigningKey(): SecretKey {
        val keyBytes = secret.toByteArray(StandardCharsets.UTF_8)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
