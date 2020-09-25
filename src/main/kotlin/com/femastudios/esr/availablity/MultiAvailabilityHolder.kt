package com.femastudios.esr.availablity

/**
 * Abstract [AvailabilityHolder] class that provides common functions for all those [AvailabilityHolder] that contain
 * other [AvailabilityHolder]s.
 */
abstract class MultiAvailabilityHolder<K, A : AvailabilityHolder>(
	val children: LinkedHashMap<K, A>,
	state: AvailabilityState = children.values.worstState()
) : AvailabilityHolder(state, children.values.mergedMessages(), children.values.minCheck()) {

	/**
	 * Will return true only if [other] is another [MultiAvailabilityHolder] with the same state and children and each
	 * pair of children's [AvailabilityHolder.isTheSame] returns true.
	 */
	override fun isTheSame(other: AvailabilityHolder): Boolean {
		return if (other !is MultiAvailabilityHolder<*, *> || state != other.state || children.keys != other.children.keys) {
			false
		} else {
			children.all { (k, v) -> v.isTheSame(other.children[k]!!) }
		}
	}

	/**
	 * All children in a [AvailabilityState.CRITICAL_ERROR] state
	 */
	fun criticalErrorChildren() = childrenByState(AvailabilityState.CRITICAL_ERROR)

	/**
	 * All children in a [AvailabilityState.ERROR] state
	 */
	fun errorChildren() = childrenByState(AvailabilityState.ERROR)

	/**
	 * All children in a [AvailabilityState.WARNING] state
	 */
	fun warningChildren() = childrenByState(AvailabilityState.WARNING)

	/**
	 * All children in a [AvailabilityState.AVAILABLE] state
	 */
	fun availableChildren() = childrenByState(AvailabilityState.AVAILABLE)

	/**
	 * Returns all the children, sorted by worst to best state
	 */
	fun childrenBySeverity(): List<A> {
		return children.values.sortedByDescending { it.state }
	}

	/**
	 * Returns all children with the given [state]
	 */
	fun childrenByState(state: AvailabilityState): LinkedHashSet<A> {
		return LinkedHashSet(children.values.filter { it.state == state })
	}
}
