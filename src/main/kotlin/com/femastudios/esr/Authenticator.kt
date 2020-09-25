package com.femastudios.esr

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.femastudios.esr.datastruct.GlobalAvailabilityComputer
import com.sun.net.httpserver.BasicAuthenticator
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpPrincipal
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.*


class Authenticator(
    private val globalAvailabilityComputer: GlobalAvailabilityComputer
) : BasicAuthenticator("get") {

    private var captureLogin = false
    private var capturedUser: String? = null

    override fun authenticate(httpExchange: HttpExchange): Result {
        val cookiesHeader = httpExchange.requestHeaders.getFirst("Cookie")
        if (cookiesHeader != null) {
            val cookies = cookiesHeader.split("\\s*;\\s*").mapNotNull {
                val parsed = it.split("=", limit = 2)
                if (parsed.size == 2) parsed[0] to parsed[1]
                else null
            }.toMap()

            val token = cookies[TOKEN_COOKIE_NAME]

            if (token != null) {
                return try {
                    val jwt = JWT_VERIFIER.verify(token)
                    Success(HttpPrincipal(jwt.getClaim("user").asString(), realm))
                } catch (exception: JWTVerificationException) {
                    Failure(401)
                } catch (exception: JWTDecodeException) {
                    Failure(401)
                }.also {
                    if (it is Failure) {
                        httpExchange.responseHeaders.add(
                            "Set-Cookie",
                            "$TOKEN_COOKIE_NAME=deleted; path=${Main.global.webServer.url.path}; Max-Age=0; Secure; HttpOnly"
                        )
                    }
                }
            }
        }
        val ret: Result
        try {
            captureLogin = true
            ret = super.authenticate(httpExchange)
            val user = capturedUser
            if (ret is Success && user != null) {
                val token = JWT.create()
                    .withIssuer(JWT_ISSUER)
                    .withClaim("user", user)
                    .withExpiresAt(Date((Instant.now() + JWT_TOKEN_EXPIRE_IN).toEpochMilli()))
                    .sign(JWT_ALGORITHM)

                val expiresInSeconds = JWT_TOKEN_EXPIRE_IN.toSeconds()
                httpExchange.responseHeaders.add(
                    "Set-Cookie",
                    "$TOKEN_COOKIE_NAME=$token; path=${Main.global.webServer.url.path}; Max-Age=$expiresInSeconds; Secure; HttpOnly"
                )
            }
            capturedUser = null
        } finally {
            captureLogin = false
        }
        return ret
    }

    override fun checkCredentials(username: String, password: String): Boolean {
        if (captureLogin) {
            capturedUser = username
        }
        return globalAvailabilityComputer.global.isUserAllowed(username, password)
    }

    companion object {
        private val JWT_ALGORITHM = run {
            val bytes = ByteArray(2000) { 0 }
            SecureRandom().nextBytes(bytes)
            Algorithm.HMAC256(bytes)
        }
        private const val JWT_ISSUER = "Easy Status Reporter"

        private val JWT_VERIFIER = JWT.require(JWT_ALGORITHM)
            .withIssuer(JWT_ISSUER)
            .acceptLeeway(5)
            .build()

        private const val TOKEN_COOKIE_NAME = "auth_token"
        private val JWT_TOKEN_EXPIRE_IN = Duration.ofDays(30)
    }
}