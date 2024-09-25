package com.kronusboss.cine.gateway.gatewayfilters

import com.kronusboss.cine.gateway.util.JWTUtil
import io.jsonwebtoken.Claims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter :
    AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config>(Config::class.java) {

    class Config

    @Autowired
    private lateinit var jwtUtil: JWTUtil

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
                val claims: Claims = jwtUtil.getClaims(token)
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
}
