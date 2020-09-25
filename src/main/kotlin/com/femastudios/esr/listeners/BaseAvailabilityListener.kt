package com.femastudios.esr.listeners

import com.femastudios.esr.availablity.AvailabilityHolder

open class BaseAvailabilityListener<A : AvailabilityHolder?> : AvailabilityListener<A> {
    override fun onAvailabilityChanged(previous: A?, current: A) {
        if (previous == null || previous.state !== current!!.state) {
            onStateChanged(previous, current)
        }
    }

    protected open fun onStateChanged(previous: A?, status: A) {}
}