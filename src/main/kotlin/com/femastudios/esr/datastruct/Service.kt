package com.femastudios.esr.datastruct

import com.femastudios.esr.availablity.AvailabilityState
import com.femastudios.esr.availablity.BaseAvailabilityChecker
import com.femastudios.esr.availablity.ServiceAvailability
import java.util.*

class Service(val group: Group<*>, val server: Server, val test: Test) :
    BaseAvailabilityChecker<ServiceAvailability>() {

    private val synchronizer = Any()

    private var totalChecksCount: Long = 0
    private var availableCount: Long = 0
    private var warningCount: Long = 0
    private var errorCount: Long = 0
    private var criticalErrorCount: Long = 0

    private val responseTimeStatistics = DoubleSummaryStatistics()

    val maxResponseTime: Double
        get() = responseTimeStatistics.max

    val minResponseTime: Double
        get() = responseTimeStatistics.min

    val averageResponseTime: Double
        get() = responseTimeStatistics.average

    init {
        require(group.servers.contains(server)) { "Server doesn't belong to group!" }
        require(group.tests.contains(test)) { "Test doesn't belong to group!" }
    }

    override fun checkAvailability(global: Global): ServiceAvailability {
        synchronized(synchronizer) {
            val serviceAvailability = try {
                test.check(global, this)
            } catch (e: InterruptedException) {
                throw IllegalStateException(e)
            }

            totalChecksCount++
            when (serviceAvailability.state) {
                AvailabilityState.AVAILABLE -> {
                    availableCount++
                    responseTimeStatistics.accept(serviceAvailability.responseTime)
                }
                AvailabilityState.WARNING -> warningCount++
                AvailabilityState.ERROR -> errorCount++
                AvailabilityState.CRITICAL_ERROR -> criticalErrorCount++
            }
            return serviceAvailability
        }
    }
}
