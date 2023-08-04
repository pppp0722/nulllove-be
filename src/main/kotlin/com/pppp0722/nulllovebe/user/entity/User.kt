package com.pppp0722.nulllovebe.user.entity

import com.pppp0722.nulllovebe.jwt.Role
import com.pppp0722.nulllovebe.user.dto.SignUpDto
import org.springframework.security.crypto.password.PasswordEncoder
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Column(unique = true)
    var userId: String,
    var password: String,
    var phone: String,
    var role: String,
    var love: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) {

    companion object {
        fun fromSignUpDto(signUpDto: SignUpDto, passwordEncoder: PasswordEncoder) =
            User(signUpDto.userId, passwordEncoder.encode(signUpDto.password), signUpDto.phone, Role.ROLE_USER.value)
    }
}
