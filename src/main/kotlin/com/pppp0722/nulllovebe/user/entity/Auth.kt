package com.pppp0722.nulllovebe.user.entity

import com.pppp0722.nulllovebe.global.util.EMAIL_AUTH_TTL
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash(value = "auth", timeToLive = EMAIL_AUTH_TTL)
data class Auth(
    @Id
    val phone: String,
    val authCode: String
)