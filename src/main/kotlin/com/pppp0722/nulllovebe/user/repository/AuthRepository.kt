package com.pppp0722.nulllovebe.user.repository

import com.pppp0722.nulllovebe.user.entity.Auth
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthRepository : CrudRepository<Auth, String> {
}