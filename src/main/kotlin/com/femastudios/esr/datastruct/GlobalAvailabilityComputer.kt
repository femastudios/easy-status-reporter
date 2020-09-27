package com.femastudios.esr.datastruct

import com.femastudios.esr.availablity.GlobalAvailability
import com.femastudios.esr.availablity.GroupAvailability
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.collections.LinkedHashSet

class GlobalAvailabilityComputer(val global: Global) {

	private val synchronizer = Any()
	private val groupStates = LinkedHashMap<Group<*>, GroupAvailability?>()
	private var currentGlobalAvailability: GlobalAvailability? = null

	private var isStarted = false

	init {
		for (group in global.groups) {
			groupStates[group] = null
		}
	}

	fun getCurrentGlobalState(onlyIfReady: Boolean = false): GlobalAvailability? {
		return if (onlyIfReady) {
			synchronized(synchronizer) {
				if (reportIsReady()) {
					currentGlobalAvailability
				} else {
					null
				}
			}
		} else {
			currentGlobalAvailability
		}
	}

	private fun reportIsReady(): Boolean {
		synchronized(synchronizer) {
			for (value in groupStates.values) {
				if (value == null) {
					return false
				}
			}
			return true
		}
	}

	private fun setGroupState(groupAvailability: GroupAvailability) {
		synchronized(synchronizer) {
			groupStates[groupAvailability.group] = groupAvailability
			currentGlobalAvailability = GlobalAvailability(global, LinkedHashSet(groupStates.values.filterNotNull()))
			global.registerNewAvailability(currentGlobalAvailability!!)
		}
	}

	fun start() {
		synchronized(synchronizer) {
			if (isStarted) {
				throw IllegalStateException()
			}
			isStarted = true
			for (group in global.groups) {
				Thread(GroupReporter(group), "Group ${group.name} reporter").start()
			}
		}
	}

	inner class GroupReporter constructor(private val group: Group<*>) : Runnable {

		override fun run() {
			while (true) {
				val start = Instant.now()
				val groupState = group.getAvailability(global)
				setGroupState(groupState)
				val waitTime = (group.refreshEvery ?: global.refreshEvery) - Duration.between(start, Instant.now())
				if (waitTime > Duration.ZERO) {
					try {
						Thread.sleep(waitTime.toMillis())
					} catch (ignored: InterruptedException) {
					}
				}
			}
		}
	}
}
