package com.pppp0722.nulllovebe.user.controller

import com.pppp0722.nulllovebe.global.util.ACCESS_TOKEN_TTL
import com.pppp0722.nulllovebe.global.util.REFRESH_TOKEN_TTL
import com.pppp0722.nulllovebe.user.dto.*
import com.pppp0722.nulllovebe.user.service.UserService
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/exists-by-userId/{userId}")
    fun existsByUserId(@PathVariable userId: String) =
        ResponseEntity.ok(userService.existsByUserId(userId))

    @GetMapping("/exists-by-phone/{phone}")
    fun existsByPhone(@PathVariable phone: String) =
        ResponseEntity.ok(userService.existsByPhone(phone))

    @PostMapping("/send-auth-code")
    fun sendAuthCode(@RequestBody sendAuthCodeDto: SendAuthCodeDto): ResponseEntity<Unit> {
        userService.sendAuthCode(sendAuthCodeDto)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/sign-up")
    fun signUp(@RequestBody signUpDto: SignUpDto): ResponseEntity<UserDto> {
        userService.signUp(signUpDto)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/login")
    fun login(@RequestBody loginDto: LoginDto): ResponseEntity<Unit> {
        val refreshToken = userService.login(loginDto)
        val cookie = ResponseCookie
            .from("Refresh-Token", refreshToken)
            .maxAge(REFRESH_TOKEN_TTL)
            .httpOnly(true)
            .path("/")
            .build()
            .toString()
        return ResponseEntity.ok().header(SET_COOKIE, cookie).build()
    }

    @GetMapping("/reissue-access-token")
    fun reissueAccessToken(
        @CookieValue(value = "Refresh-Token", defaultValue = "")
        refreshToken: String
    ): ResponseEntity<Unit> {
        val accessToken = userService.reissueAccessToken(refreshToken)
        val cookie = ResponseCookie
            .from("Access-Token", accessToken)
            .maxAge(ACCESS_TOKEN_TTL)
            .httpOnly(true)
            .path("/")
            .build()
            .toString()
        return ResponseEntity.ok().header(SET_COOKIE, cookie).build()
    }

    @PatchMapping("/love")
    fun setLove(@AuthenticationPrincipal userId: String, @RequestBody updateDto: UpdateDto): ResponseEntity<Unit> {
        userService.setLove(userId, updateDto)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/love-info")
    fun getLoveInfo(@AuthenticationPrincipal userId: String) =
        ResponseEntity.ok(userService.getLoveInfo(userId))
}
