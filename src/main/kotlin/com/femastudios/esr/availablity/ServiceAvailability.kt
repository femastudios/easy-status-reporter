package com.femastudios.esr.availablity

import com.femastudios.esr.datastruct.Service
import java.time.Instant

class ServiceAvailability(
    message: String,
    checkTime: Instant,
    val service: Service,
    state: AvailabilityState,
    val responseTime: Double
) : AvailabilityHolder(state, message, checkTime) {

    override fun isTheSame(other: AvailabilityHolder): Boolean {
        return state == other.state && message == other.message
    }
}
