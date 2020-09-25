package com.femastudios.esr.availablity

import com.femastudios.esr.datastruct.Server
import com.femastudios.esr.datastruct.Service
import java.util.*

/**
 * Represents the availability of a single [Server]
 * @param server The server
 * @param serviceAvailabilities The availabilities of the services of the server
 */
class ServerAvailability(
    val server: Server,
    serviceAvailabilities: LinkedHashSet<ServiceAvailability>
) : MultiAvailabilityHolder<Service, ServiceAvailability>(serviceAvailabilities.associateByTo(LinkedHashMap()) { it.service })