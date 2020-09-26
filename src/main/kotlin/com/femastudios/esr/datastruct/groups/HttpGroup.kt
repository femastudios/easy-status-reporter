package com.femastudios.esr.datastruct.groups

import com.femastudios.esr.availablity.AvailabilityState
import com.femastudios.esr.availablity.BaseAvailabilityChecker
import com.femastudios.esr.availablity.GroupAvailability
import com.femastudios.esr.availablity.ServerAvailability
import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.datastruct.Group
import com.femastudios.esr.datastruct.Server
import com.femastudios.esr.datastruct.ServiceProvider
import com.femastudios.esr.datastruct.tests.HttpTest
import com.femastudios.esr.util.executeParallely
import org.apache.commons.jexl3.JexlExpression
import java.time.Duration
import java.util.*
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap

data class HttpGroup(
    override val name: String,
    override val timeoutIn: Duration? = null,
    override val refreshEvery: Duration? = null,
    override val criticalWhen: JexlExpression = Group.DEFAULT_CRITICAL_ERROR_CONDITION,
    override val errorWhen: JexlExpression = Group.DEFAULT_ERROR_CONDITION,
    override val warningWhen: JexlExpression = Group.DEFAULT_WARNING_CONDITION,
    override val servers: List<Server>,
    override val tests: List<HttpTest> = listOf(HttpTest())
) : Group<HttpTest>, BaseAvailabilityChecker<GroupAvailability>() {

    override val serviceProvider = ServiceProvider(this)

    private val synchronizer = Any()

    override fun checkAvailability(global: Global): GroupAvailability {
        synchronized(synchronizer) {
            val runnables: MutableSet<Runnable> = HashSet()
            //using a Map to preserve the order of the servers
            val reports = Collections.synchronizedMap(LinkedHashMap<Server, ServerAvailability>())!!
            for (server in servers) {
                runnables.add(Runnable { reports[server] = server.getAvailability(global) })
            }
            try {
                runnables.executeParallely("Server checker thread")
            } catch (e: InterruptedException) {
                throw IllegalStateException(e)
            }
            return GroupAvailability(this, LinkedHashSet<ServerAvailability>(reports.values))
        }
    }
}