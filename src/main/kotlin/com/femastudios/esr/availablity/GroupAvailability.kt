package com.femastudios.esr.availablity

import com.femastudios.esr.availablity.AvailabilityState.*
import com.femastudios.esr.datastruct.Group
import com.femastudios.esr.datastruct.Server
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
                    group.criticalWhen.isMet(servers) -> CRITICAL_ERROR
                    group.errorWhen.isMet(servers) -> ERROR
                    group.warningWhen.isMet(servers) -> WARNING
                    else -> AVAILABLE
                }
            )
        }
    }
}

private fun JexlExpression.isMet(servers: Set<ServerAvailability>): Boolean {
    //TODO add variables
    val jc = MapContext().apply {
        this["error.count"] = servers.count { s -> s.state == ERROR }
        this["warning.count"] = servers.count { s -> s.state == WARNING }
        this["not_available.count"] = servers.count { s -> s.state != AVAILABLE }
        this["available.count"] = servers.count { s -> s.state == AVAILABLE }
        this["server.count"] = servers.size
    }
    return evaluate(jc) as Boolean //TODO handle not booleans

}
