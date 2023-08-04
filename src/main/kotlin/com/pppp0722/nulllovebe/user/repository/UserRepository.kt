package com.pppp0722.nulllovebe.user.repository

import com.pppp0722.nulllovebe.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun existsByUserId(userId: String): Boolean

    fun existsByPhone(phone: String): Boolean

    fun findByUserId(userId: String): User?

    fun findByLove(like: String): List<User>
}