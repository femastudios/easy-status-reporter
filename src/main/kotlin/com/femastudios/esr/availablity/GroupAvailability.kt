package com.femastudios.esr.availablity

import com.femastudios.esr.availablity.AvailabilityState.*
import com.femastudios.esr.datastruct.Group
import com.femastudios.esr.datastruct.Server
import com.femastudios.esr.util.evaluateBoolean
import org.apache.commons.jexl3.JexlExpression
import org.apache.commons.jexl3.MapContext
import java.util.*

class GroupAvailability(
    val group: Group<*>,
    servers: LinkedHashSet<ServerAvailability>
) : MultiAvailabilityHolder<Server, ServerAvailability>(
    servers.associateByTo(LinkedHashMap()) { it.server },
    computeState(group, servers)
) {

    companion object {
        fun computeState(group: Group<*>, servers: Set<ServerAvailability>): AvailabilityState {
            return minOf(
                group.worstPossibleState,
                when {
                    group.criticalWhen.isMet(servers, "critical-when") -> CRITICAL_ERROR
                    group.errorWhen.isMet(servers, "error-when") -> ERROR
                    group.warningWhen.isMet(servers, "warning-when") -> WARNING
                    else -> AVAILABLE
                }
            )
        }
    }
}

private fun JexlExpression.isMet(servers: Set<ServerAvailability>, name : String): Boolean {
    //TODO add variables
    val jc = MapContext().apply {
        this["error.count"] = servers.count { s -> s.state == ERROR }
        this["warning.count"] = servers.count { s -> s.state == WARNING }
        this["not_available.count"] = servers.count { s -> s.state != AVAILABLE }
        this["available.count"] = servers.count { s -> s.state == AVAILABLE }
        this["server.count"] = servers.size
    }
    return evaluateBoolean(jc, name)

}
