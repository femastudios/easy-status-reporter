package com.femastudios.esr.listeners

import com.femastudios.esr.Main
import com.femastudios.esr.ShutdownDetector
import mu.KotlinLogging
import java.time.Instant

private val logger = KotlinLogging.logger {}

object ShutdownLoggerListener : ShutdownDetector.OnShutdownAnomaly {

    override fun onShutdownAnomaly(estimateCrashTime: Instant) {
        logger.warn(
            "Shutdown Anomaly detected! Estimated crash time: %s".format(
                estimateCrashTime.atZone(Main.global.timezone).toString()
            )
        )
    }
}
