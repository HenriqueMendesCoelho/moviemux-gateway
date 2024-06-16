package com.kronusboss.cine.gateway.gatewayfilters

import io.jsonwebtoken.Claims
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class RoleAdmAuthorizationFilter : AbstractGatewayFilterFactory<RoleAdmAuthorizationFilter.Config>(Config::class.java) {

    class Config


    override fun apply(config: Config?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val claims = exchange.attributes["claims"] as Claims?

            if (claims == null || !claims.get("roles", List::class.java).contains("ADM")) {
                exchange.response.statusCode = HttpStatus.FORBIDDEN
                exchange.response.setComplete()
            } else {
                chain.filter(exchange)
            }
        }
    }

}
