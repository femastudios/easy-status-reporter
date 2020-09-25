package com.femastudios.esr.availablity

import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.listeners.AvailabilityListener
import java.util.*

/**
 * Abstract class that shall be extended by classes that want to report an availability status.
 * @param A The type of [AvailabilityHolder] that this class will report.
 */
abstract class BaseAvailabilityChecker<A : AvailabilityHolder> : AvailabilityChecker<A> {

    final override var lastSignificantAvailability: A? = null; private set

	final override var lastAvailability: A? = null; private set


    private val onAvailabilityChangesListeners = HashSet<AvailabilityListener<in A>>()

    /**
     * Checks the current availability and returns it
     */
    override fun getAvailability(global: Global): A {
        val availability = checkAvailability(global)
        registerNewAvailability(availability)
        return availability
    }

    /**
     * Registers a new availability in this class and calls the listeners
     */
    protected open fun registerNewAvailability(availability: A) {
        val previous = this.lastAvailability
        lastAvailability = availability
        if (lastSignificantAvailability == null || lastSignificantAvailability!!.state !== availability.state) {
            lastSignificantAvailability = availability
        }
        for (onAvailabilityChangesListener in onAvailabilityChangesListeners) {
            onAvailabilityChangesListener.onAvailabilityChanged(previous, availability)
        }
    }

	override fun addOnAvailabilityChanges(onAvailabilityChanges: AvailabilityListener<in A>) {
        this.onAvailabilityChangesListeners.add(onAvailabilityChanges)
    }

    /**
     * Abstract function that checks the availability an returns it.
     * @param global The global settings
     */
    protected abstract fun checkAvailability(global: Global): A
}
