package com.ajaysmarketplace.auth

import com.ajaysmarketplace.user.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val cookieService: CookieService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // read the token from the HttpOnly cookie — not from the header
        val token = cookieService.extractAccessToken(request)

        // no cookie present — pass through unauthenticated
        // Spring Security will return 401 if the endpoint requires auth
        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        // token present but invalid (expired, tampered with, wrong type)
        if (!jwtService.isTokenValid(token) || jwtService.isRefreshToken(token)) {
            filterChain.doFilter(request, response)
            return
        }

        // load the user from DB — confirms account still exists
        val userId = jwtService.extractUserId(token)
        val user = userRepository.findById(userId).orElse(null) ?: run {
            filterChain.doFilter(request, response)
            return
        }

        // tell Spring Security this request is authenticated
        val authentication = UsernamePasswordAuthenticationToken(
            user,
            null,
            listOf(SimpleGrantedAuthority(user.role))
        )
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }
}