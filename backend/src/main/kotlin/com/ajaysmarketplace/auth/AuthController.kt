package com.ajaysmarketplace.auth

import com.ajaysmarketplace.user.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieService: CookieService
) {

    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest,
        response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {
        val result = authService.register(request)
        cookieService.setAuthCookies(response, result.accessToken, result.refreshToken)
        // tokens go into HttpOnly cookies
        // user info goes into response body
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.buildUserResponse(result.user))
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {
        val result = authService.login(request)
        cookieService.setAuthCookies(response, result.accessToken, result.refreshToken)
        return ResponseEntity.ok(authService.buildUserResponse(result.user))
    }

    @PostMapping("/refresh")
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<AuthResponse> {
        // read the refresh token from its cookie
        val refreshToken = cookieService.extractRefreshToken(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val result = authService.refresh(refreshToken)
        cookieService.setAuthCookies(response, result.accessToken, result.refreshToken)
        return ResponseEntity.ok(authService.buildUserResponse(result.user))
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Void> {
        cookieService.clearAuthCookies(response)
        // clears both cookies — user is logged out
        return ResponseEntity.noContent().build()
    }
}