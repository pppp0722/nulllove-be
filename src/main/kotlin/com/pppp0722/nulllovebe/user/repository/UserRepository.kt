package com.pppp0722.nulllovebe.user.repository

import com.pppp0722.nulllovebe.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import javax.persistence.LockModeType

@Repository
interface UserRepository : JpaRepository<User, Long>{

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    fun findByUserIdWithLock(userId: String): User?
}