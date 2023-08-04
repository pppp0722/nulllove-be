package com.pppp0722.nulllovebe.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("jwt")
data class JwtProperties(
    val issuer: String,
    val clientSecret: String,
    val accessTokenTtl: Long,
    val refreshTokenTtl: Long
)