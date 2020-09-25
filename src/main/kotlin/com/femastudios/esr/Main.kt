package com.femastudios.esr

import com.femastudios.esr.availablity.GroupAvailability
import com.femastudios.esr.availablity.ServerAvailability
import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.datastruct.GlobalAvailabilityComputer
import com.femastudios.esr.datastruct.Server
import com.femastudios.esr.listeners.AvailabilityLoggerListener
import com.femastudios.esr.listeners.ShutdownLoggerListener
import mu.KotlinLogging
import org.slf4j.impl.SimpleLogger
import java.io.File
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

object Main {
    private val CONFIG_FILE = File("config.yml")

    lateinit var global: Global; private set
    lateinit var globalAvailabilityComputer: GlobalAvailabilityComputer; private set

    @JvmStatic
    fun main(args: Array<String>) {
        if (!CONFIG_FILE.exists()) {
            logger.error("Config file not found! Please create file named config.yml in " + CONFIG_FILE.absoluteFile.parent)
            exitProcess(1)
        }
        global = Global.load(CONFIG_FILE)
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, global.logLevel.name)
        globalAvailabilityComputer = GlobalAvailabilityComputer(global)

        logger.info("Welcome to EASY STATUS REPORTER!")
        logger.info("Current log level: " + global.logLevel)

        logger.debug("Registering shutdown detector...")
        val shutdownDetector = ShutdownDetector()
        shutdownDetector.addOnShutdownAnomalyListener(ShutdownLoggerListener)

        logger.debug("Registering group listeners...")
        val groupListener = AvailabilityLoggerListener<GroupAvailability> { it.group.name }
        for (it in global.groups) {
            it.addOnAvailabilityChanges(groupListener)
        }
        global.addOnAvailabilityChanges(AvailabilityLoggerListener { "GLOBAL" })

        logger.debug("Registering server listeners...")
        val serverListener = AvailabilityLoggerListener<ServerAvailability> { it.server.displayName }
        for (group in global.groups) {
            for (server: Server in group.servers) {
                server.addOnAvailabilityChanges(serverListener)
            }
        }

        logger.debug("Starting agents...")
        for (agent in global.agents) {
            agent.start()
            global.addOnAvailabilityChanges(agent)
            shutdownDetector.addOnShutdownAnomalyListener(agent)
        }

        //Starting services
        logger.debug("Starting services...")
        WebServer.start(globalAvailabilityComputer)
        globalAvailabilityComputer.start()
        shutdownDetector.register()
        logger.info("Server started, bound to " + global.webServer.bind.hostAddress + ":" + global.webServer.port)
    }
}