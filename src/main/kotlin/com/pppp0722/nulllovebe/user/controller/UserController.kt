package com.pppp0722.nulllovebe.user.controller

import com.pppp0722.nulllovebe.user.dto.SendAuthCodeDto
import com.pppp0722.nulllovebe.user.dto.SignUpDto
import com.pppp0722.nulllovebe.user.dto.UserDto
import com.pppp0722.nulllovebe.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody signUpDto: SignUpDto): ResponseEntity<UserDto> {
        val userDto = userService.signUp(signUpDto)
        return ResponseEntity.ok(userDto)
    }

    @PostMapping("/send-auth-code")
    fun sendAuthCode(@RequestBody sendAuthCodeDto: SendAuthCodeDto): ResponseEntity<Unit> {
        userService.sendAuthCode(sendAuthCodeDto)
        return ResponseEntity.ok().build()
    }
}