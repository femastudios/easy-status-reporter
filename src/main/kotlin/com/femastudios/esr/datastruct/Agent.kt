package com.femastudios.esr.datastruct

import com.femastudios.esr.ShutdownDetector
import com.femastudios.esr.availablity.GlobalAvailability
import com.femastudios.esr.listeners.AvailabilityListener

interface Agent : AvailabilityListener<GlobalAvailability>, ShutdownDetector.OnShutdownAnomaly {
    val debounce: DebouncingInfo?

    fun start()
}

