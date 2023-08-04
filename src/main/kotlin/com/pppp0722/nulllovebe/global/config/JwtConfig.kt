package com.pppp0722.nulllovebe.global.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.pppp0722.nulllovebe.jwt.JwtGenerator
import com.pppp0722.nulllovebe.global.properties.JwtProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig(
    private val jwtProperties: JwtProperties
) {

    private val algorithm = Algorithm.HMAC512(jwtProperties.clientSecret)

    @Bean
    fun jwtGenerator(): JwtGenerator =
        JwtGenerator(
            jwtProperties.issuer,
            algorithm,
            jwtProperties.accessTokenTtl,
            jwtProperties.refreshTokenTtl
        )

    @Bean
    fun jwtVerifier(): JWTVerifier =
        JWT.require(algorithm)
            .withIssuer(jwtProperties.issuer)
            .build()
}
