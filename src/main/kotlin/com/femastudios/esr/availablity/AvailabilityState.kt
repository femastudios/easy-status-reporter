package com.femastudios.esr.availablity

/**
 * Enumerator that represents the state of a server or service.
 * States are sorted from best to worst.
 *
 * @property cssClassname The CSS class name that styles this state
 * @property humanName A human-readable name for this state
 * @property configName The name of this state in the config file
 * @property symbol A symbol that represents this state
 */
enum class AvailabilityState(val cssClassname: String, val humanName: String, val configName: String, val symbol: Char) {
    AVAILABLE("available", "available", "available", '✔'),
    WARNING("warning", "warning", "warning", '⚠'),
    ERROR("error", "error", "error", '❌'),
    CRITICAL_ERROR("critical-error", "critical error", "critical-error", '☢')
}

/**
 * Returns the worst [AvailabilityState] in the iterable.
 * @throws NoSuchElementException if the iterable is empty
 */
fun Iterable<AvailabilityState>.worst(): AvailabilityState = max() ?: throw NoSuchElementException("Empty collection")

/**
 * Returns the best [AvailabilityState] in the iterable.
 * @throws NoSuchElementException if the iterable is empty
 */
fun Iterable<AvailabilityState>.best(): AvailabilityState = min() ?: throw NoSuchElementException("Empty collection")