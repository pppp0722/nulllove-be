package com.pppp0722.nulllovebe.user.repository

import com.pppp0722.nulllovebe.user.entity.RefreshToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : CrudRepository<RefreshToken, String> {
}