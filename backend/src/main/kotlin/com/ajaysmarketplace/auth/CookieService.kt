package com.ajaysmarketplace.auth

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class CookieService(
    @Value("\${jwt.access-token-expiry}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long,
    @Value("\${cookie.secure:true}") private val secureCookie: Boolean

) {
    companion object {
        const val ACCESS_COOKIE = "access_token"
        const val REFRESH_COOKIE = "refresh_token"
    }

    fun setAuthCookies(response: HttpServletResponse, accessToken: String, refreshToken: String) {
        response.addCookie(buildCookie(ACCESS_COOKIE, accessToken, (accessTokenExpiry / 1000).toInt()))
        response.addCookie(buildCookie(REFRESH_COOKIE, refreshToken, (refreshTokenExpiry / 1000).toInt()))
        // maxAge is in seconds, our expiry values are in milliseconds — divide by 1000
    }

    fun clearAuthCookies(response: HttpServletResponse) {
        // setting maxAge to 0 tells the browser to delete the cookie immediately
        response.addCookie(buildCookie(ACCESS_COOKIE, "", 0))
        response.addCookie(buildCookie(REFRESH_COOKIE, "", 0))
    }

    fun extractAccessToken(request: HttpServletRequest): String? =
        extractCookie(request, ACCESS_COOKIE)

    fun extractRefreshToken(request: HttpServletRequest): String? =
        extractCookie(request, REFRESH_COOKIE)

    private fun extractCookie(request: HttpServletRequest, name: String): String? =
        request.cookies?.find { it.name == name }?.value

    private fun buildCookie(name: String, value: String, maxAgeSeconds: Int): Cookie =

        Cookie(name, value).apply {
            isHttpOnly = true
            // HttpOnly = JavaScript cannot read this cookie at all
            // this is the entire XSS protection — even if an attacker
            // runs JS on your page, they can't steal the token

            secure = true
            // only sent over HTTPS — prevents cookie being sent over plain HTTP
            // NOTE: for local dev this will cause issues since localhost is HTTP
            // we'll handle that with a dev profile below

            path = "/"
            // cookie is sent on ALL requests to your domain, not just /auth

            this.maxAge = maxAgeSeconds
            // how long the browser keeps this cookie
            secure = secureCookie

        }
}