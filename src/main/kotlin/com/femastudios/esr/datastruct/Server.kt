package com.femastudios.esr.datastruct

import com.femastudios.esr.availablity.BaseAvailabilityChecker
import com.femastudios.esr.availablity.ServerAvailability
import com.femastudios.esr.availablity.ServiceAvailability
import com.femastudios.esr.util.executeParallely
import java.net.InetAddress
import java.util.*
import kotlin.collections.HashSet

data class Server(
    val address: InetAddress,
    val name: String? = null
) : BaseAvailabilityChecker<ServerAvailability>() {

    val displayName: String
        get() = name ?: address.hostName


    private val synchronizer = Any()
    override fun checkAvailability(global: Global): ServerAvailability {
        synchronized(synchronizer) {
            val group = global.getBelongingGroup(this)

            val reports = Collections.synchronizedMap(LinkedHashMap<Test, ServiceAvailability>())

            val runnables = group.tests.mapTo(HashSet()) { test ->
                Runnable { reports[test] = group.serviceProvider.getService(this, test).getAvailability(global) }
            }
            try {
                runnables.executeParallely("Test checker thread")
            } catch (e: InterruptedException) {
                throw IllegalStateException(e)
            }
            return ServerAvailability(this, LinkedHashSet(reports.values))
        }
    }
}