package com.femastudios.esr.availablity

import java.time.Instant

/**
 * Abstract class that keeps an [AvailabilityState] along with other information.
 * Implementations must be immutable.
 *
 * @property state The state
 * @property message A message
 * @property checkTime The time of the check that produced this state
 */
abstract class AvailabilityHolder(
    val state: AvailabilityState,
    val message: String,
    val checkTime: Instant
) : Comparable<AvailabilityHolder> {

    override fun toString(): String {
        return "$state $message"
    }

    override fun compareTo(other: AvailabilityHolder): Int {
        return -state.compareTo(other.state)
    }

    /**
     * Function that must return whether this and another [AvailabilityHolder] represent the same status.
     * Note that for this to happen it's not necessary that all properties are the same.
     */
    open fun isTheSame(other: AvailabilityHolder): Boolean {
        return state == other.state
    }
}


/**
 * Returns a string of concatenated messages for the iterable of [AvailabilityHolder].
 * If two or more messages are identical, only one is kept.
 */
fun Iterable<AvailabilityHolder>.mergedMessages(): String = mapTo(HashSet()) { it.message }.joinToString("; ")

/**
 * Returns an [Instant] representing the minimum check time for the iterable of [AvailabilityHolder].
 * @throws NoSuchElementException if the iterable is empty
 */
fun Iterable<AvailabilityHolder>.minCheck(): Instant = minByOrNull { it.checkTime }?.checkTime ?: throw NoSuchElementException("Empty collection")

/**
 * Returns the worst state in the iterable of [AvailabilityHolder].
 * @throws NoSuchElementException if the iterable is empty
 */
fun Iterable<AvailabilityHolder>.worstState(): AvailabilityState = map { it.state }.worst()

/**
 * Returns the best state in the iterable of [AvailabilityHolder].
 * @throws NoSuchElementException if the iterable is empty
 */
fun Iterable<AvailabilityHolder>.bestState(): AvailabilityState = map { it.state }.best()

