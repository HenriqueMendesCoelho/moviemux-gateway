package com.moviemux.gateway.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

@Component
class JWTUtil {

    @Value("\${jwt.secret}")
    lateinit var secret: String
    fun getClaims(token: String): Claims? {
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
