package com.femastudios.esr.listeners

import com.femastudios.esr.availablity.AvailabilityHolder

/**
 * Listener interface for listening to changes in availability
 */
interface AvailabilityListener<A : AvailabilityHolder?> {

    /**
     * Called when the availability changes
     * @param previous the previous availability. Null If not present.
     * @param current the current availability.
     */
    fun onAvailabilityChanged(previous: A?, current: A)
}