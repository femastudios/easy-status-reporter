package com.femastudios.esr

import com.femastudios.esr.availablity.GroupAvailability
import com.femastudios.esr.availablity.ServerAvailability
import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.datastruct.GlobalAvailabilityComputer
import com.femastudios.esr.datastruct.Server
import com.femastudios.esr.listeners.AvailabilityLoggerListener
import com.femastudios.esr.listeners.ShutdownLoggerListener
import com.femastudios.esr.util.getBuildVersion
import mu.KotlinLogging
import org.slf4j.impl.SimpleLogger
import java.io.File
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

object Main {
    val CONFIG_DIR: File = File(System.getProperty("com.femastudios.esr.configDir", File("").absolutePath))
    val CONFIG_FILE = File(CONFIG_DIR, "config.yml")

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

        logger.info("Welcome to EASY STATUS REPORTER v" + getBuildVersion())
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
        try {
            WebServer.start(globalAvailabilityComputer)
        } catch (e: Exception) {
            when (e) {
                is IOException, is KeyManagementException, is NoSuchAlgorithmException -> {
                    logger.error(e) { "Unable to start web server" }
                    exitProcess(1)
                }
                else -> throw e
            }
        }
        logger.info("Server started, bound to " + global.webServer.socketAddress.toString())
        globalAvailabilityComputer.start()
        shutdownDetector.register()
    }
}