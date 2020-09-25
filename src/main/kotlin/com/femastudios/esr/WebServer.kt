package com.femastudios.esr

import com.femastudios.esr.availablity.GlobalAvailability
import com.femastudios.esr.availablity.GroupAvailability
import com.femastudios.esr.availablity.ServerAvailability
import com.femastudios.esr.availablity.ServiceAvailability
import com.femastudios.esr.datastruct.GlobalAvailabilityComputer
import com.femastudios.esr.datastruct.HTMLTemplate
import com.femastudios.esr.datastruct.Test
import com.femastudios.esr.util.format
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import mu.KotlinLogging
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException

private val logger = KotlinLogging.logger {}

class WebServer private constructor(private val globalAvailabilityComputer: GlobalAvailabilityComputer) : HttpHandler {

    @Throws(IOException::class)
    override fun handle(t: HttpExchange) {
        try {
            val snapshot = globalAvailabilityComputer.getCurrentGlobalState(true)
            if (snapshot == null) {
                sendResponse(t, 500, "Report not ready yet")
            } else {
                sendResponse(t, 200, GLOBAL_TEMPLATE.apply(snapshot))
            }
        } catch (e: Exception) {
            logger.error(e) { "Error handling web request" }
        }
    }

    @Throws(IOException::class)
    fun sendResponse(t: HttpExchange, responseCode: Int, response: String) {
        t.sendResponseHeaders(responseCode, response.length.toLong())
        val os = t.responseBody
        os.write(response.toByteArray())
        os.close()
    }

    companion object {
        private val BASE_GROUP_TEMPLATE = HTMLTemplate<GroupAvailability>(
			"base-group-template.html",
			{ g -> g.group.name },
			{ g -> g.state.cssClassname },
			{ g -> g.state.humanName },
			{ g -> g.message },
			{ g -> g.checkTime.atZone(Main.global.timezone) },
			{ g -> g.group.timeSinceLastSignificantAvailabilityChange?.format() }
		)
        private val SERVICE_TEMPLATE = HTMLTemplate<ServiceAvailability>(
			"service-template.html",
			{ s -> s.state.cssClassname },
			{ s -> s.state.humanName },
			{ s -> s.message },
			{ s -> s.checkTime.atZone(Main.global.timezone) },
			{ g -> g.service.timeSinceLastSignificantAvailabilityChange?.format() }
		)
        private val SERVER_TEMPLATE = HTMLTemplate<ServerAvailability>(
			"server-template.html",
			{ s -> s.server.displayName },
			{ s -> s.server.address.hostName },
			{ s -> s.state.cssClassname },
			{ s -> s.state.humanName },
			{ s -> s.message },
			{ s -> s.checkTime.atZone(Main.global.timezone) },
			{ g -> g.server.timeSinceLastSignificantAvailabilityChange?.format() },
			{ s -> SERVICE_TEMPLATE.applyMultiple(s.children.values) }
		)
        private val TESTS_TEMPLATE = HTMLTemplate<Test>(
			"test-path-template.html",
			{ it.displayName },
			{ it.name }
		)
        private val COMPLETE_GROUP_TEMPLATE = HTMLTemplate<GroupAvailability>(
			"group-template.html",
			{ g -> g.group.name },
			{ g -> g.state.cssClassname },
			{ g -> g.state.humanName },
			{ g -> g.message },
			{ g -> g.checkTime.atZone(Main.global.timezone) },
			{ g -> g.group.timeSinceLastSignificantAvailabilityChange?.format() },
			{ g -> TESTS_TEMPLATE.applyMultiple(g.group.tests) },
			{ g -> SERVER_TEMPLATE.applyMultiple(g.childrenBySeverity()) }
		)
        private val GLOBAL_TEMPLATE = HTMLTemplate<GlobalAvailability>(
			"template.html",
			{ g -> g.global.webServer.title },
			{ g -> g.state.cssClassname },
			{ g -> g.state.humanName },
			{ g -> g.message },
			{ g -> g.checkTime.atZone(Main.global.timezone) },
			{ g -> g.global.timeSinceLastSignificantAvailabilityChange?.format() },
			{ g -> BASE_GROUP_TEMPLATE.applyMultiple(g.criticalErrorChildren()) },
			{ g -> BASE_GROUP_TEMPLATE.applyMultiple(g.errorChildren()) },
			{ g -> BASE_GROUP_TEMPLATE.applyMultiple(g.warningChildren()) },
			{ g -> BASE_GROUP_TEMPLATE.applyMultiple(g.availableChildren()) },
			{ g -> COMPLETE_GROUP_TEMPLATE.applyMultiple(g.childrenBySeverity()) }
		)

        @Throws(IOException::class, KeyManagementException::class, NoSuchAlgorithmException::class)
        fun start(globalAvailabilityComputer: GlobalAvailabilityComputer) {
            val server = HttpServer.create(globalAvailabilityComputer.global.webServer.socketAddress, 0)
            val context = server.createContext("/", WebServer(globalAvailabilityComputer))
            val authenticator = Authenticator(globalAvailabilityComputer)
            context.authenticator = authenticator
            server.executor = null
            server.start()
        }
    }

}
