package com.pppp0722.nulllovebe.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtGenerator(
    private val issuer: String,
    private val algorithm: Algorithm,
    private val accessTokenTtl: Long,
    private val refreshTokenTtl: Long
) {

    fun generateRefreshToken(userId: String, roles: Array<String>) =
        generateToken(userId, roles, refreshTokenTtl)

    fun generateAccessToken(userId: String, roles: Array<String>) =
        generateToken(userId, roles, accessTokenTtl)

    private fun generateToken(userId: String, roles: Array<String>, ttl: Long): String {
        val now = Date()
        val builder = JWT.create()
        builder.withIssuer(issuer)
        builder.withIssuedAt(now)
        builder.withExpiresAt(Date(now.time + ttl * 1000L))
        builder.withClaim("aud", userId)
        builder.withArrayClaim("roles", roles)
        return builder.sign(algorithm)
    }
}