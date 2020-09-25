package com.femastudios.esr.listeners

import com.femastudios.esr.availablity.AvailabilityHolder
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class AvailabilityLoggerListener<A : AvailabilityHolder?>(
    val nameObtainer: (A) -> String
) : BaseAvailabilityListener<A>() {

    override fun onStateChanged(previous: A?, status: A) {
        logger.info(nameObtainer(status) + ": " + status)
    }
}