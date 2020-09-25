package com.femastudios.esr.availablity

import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.listeners.AvailabilityListener

import java.time.Duration
import java.time.Instant

interface AvailabilityChecker<A : AvailabilityHolder> {

	/**
	 * The last reported availability for which the status changed
	 */
	val lastSignificantAvailability: A?

	/**
	 * The last reported availability
	 */
	val lastAvailability: A?


	/**
	 * The time passed since the last change in availability.
	 * Null when the availability has never been reported.
	 */
	val timeSinceLastSignificantAvailabilityChange: Duration?
		get() = if (lastSignificantAvailability != null) {
			Duration.between(lastSignificantAvailability!!.checkTime, Instant.now())
		} else null

	/**
	 * Checks the current availability and returns it
	 */
	fun getAvailability(global: Global): A

	fun addOnAvailabilityChanges(onAvailabilityChanges: AvailabilityListener<in A>)

}
