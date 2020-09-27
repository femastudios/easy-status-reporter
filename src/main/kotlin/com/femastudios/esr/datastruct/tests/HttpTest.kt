package com.femastudios.esr.datastruct.tests

import com.femastudios.esr.availablity.AvailabilityState
import com.femastudios.esr.availablity.ServiceAvailability
import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.datastruct.Service
import com.femastudios.esr.datastruct.Test
import com.femastudios.esr.util.*
import mu.KotlinLogging
import org.apache.commons.jexl3.JexlExpression
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.time.Instant
import java.util.*
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger { }

data class HttpTest(
    override val name: String? = null,
    val protocol: Protocol = Protocol.HTTP,
    val port: Int = protocol.defaultPort,
    val path: String = "/",
    val method: String = "GET",
    val headers: Map<String, String> = emptyMap(),
    val body: File? = null,
    override val criticalWhen: JexlExpression = JEXL_ENGINE.createExpression("never"),
    override val errorWhen: JexlExpression = JEXL_ENGINE.createExpression("response.code != 200"),
    override val warningWhen: JexlExpression = JEXL_ENGINE.createExpression("never")
) : Test {

    override val displayName: String
        get() = name ?: path

    enum class Protocol(val defaultPort: Int) { HTTP(80), HTTPS(443) }

    override fun check(global: Global, service: Service): ServiceAvailability {
        val realTimeout = service.group.timeoutIn ?: global.timeoutIn
        val now = Instant.now()

        var resultTemp: Pair<AvailabilityState, String>? = null
        var connectionReference: HttpURLConnection? = null
        val thread = thread(name = "HTTP Test $displayName") {
            resultTemp = try {
                val url = URL(protocol.name.toLowerCase(Locale.ROOT), service.server.address.hostName, port, path)
                val connection = url.openConnection() as HttpURLConnection
                connectionReference = connection

                connection.requestMethod = method
                connection.connectTimeout = realTimeout.toMillis().toInt()
                connection.readTimeout = realTimeout.toMillis().toInt()
                for ((key, value) in headers) {
                    connection.setRequestProperty(key, value)
                }
                if (body != null) {
                    connection.doOutput = true
                    body.inputStream().transferTo(connection.outputStream)
                }

                val context = mapOf(
                    "response.code" to connection.responseCode,
                    "response.message" to connection.responseMessage,
                    "response.headers" to connection.headerFields
                ).createJexlContext()

                val response = "(Response is ${connection.responseCode} ${connection.responseMessage})"
                when {
                    errorWhen.evaluateBoolean(context, "error-when") -> {
                        AvailabilityState.ERROR to "Error condition is met $response"
                    }
                    warningWhen.evaluateBoolean(context, "warning-when") -> {
                        AvailabilityState.WARNING to "Warning condition is met $response"
                    }
                    else -> AvailabilityState.AVAILABLE to "Available"
                }
            } catch (e: SocketTimeoutException) {
                null
            } catch (e: IOException) {
                AvailabilityState.ERROR to e.javaClass.simpleName + ": " + e.message
            } catch (je: JexlEvaluationException) {
                logger.error(je) { je.message }
                AvailabilityState.CRITICAL_ERROR to je.message
            }
        }
        thread.join(realTimeout.toMillis())
        thread.interrupt()
        connectionReference?.disconnect()
        val result = resultTemp ?: AvailabilityState.ERROR to "Timeout of ${realTimeout.format()} reached"

        logger.debug {
            "Test %s/%s/%s completed with result: %s, %s".format(
                service.group.name,
                service.server.displayName,
                displayName,
                result.first.toString(),
                result.second
            )
        }
        return ServiceAvailability(
            message = result.second,
            checkTime = now,
            service = service,
            state = result.first,
            responseTime = (System.currentTimeMillis() - now.toEpochMilli()).toDouble()
        )
    }

}