package com.pppp0722.nulllovebe.user.entity

import com.pppp0722.nulllovebe.user.dto.SignUpDto
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(unique = true)
    var userId: String,
    var password: String,
    var phone: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {
    companion object {
        fun fromSignUpDto(signUpDto: SignUpDto) =
            User(signUpDto.userId, signUpDto.password, signUpDto.phone)
    }
}