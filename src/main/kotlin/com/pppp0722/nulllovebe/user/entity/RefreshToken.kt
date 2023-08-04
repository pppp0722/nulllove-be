package com.pppp0722.nulllovebe.user.entity

import com.pppp0722.nulllovebe.global.util.REFRESH_TOKEN_TTL
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash(value = "refresh_token", timeToLive = REFRESH_TOKEN_TTL)
data class RefreshToken(
    @Id
    val userId: String,
    val value: String
)