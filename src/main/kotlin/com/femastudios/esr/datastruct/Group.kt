package com.femastudios.esr.datastruct

import com.femastudios.esr.util.JEXL_ENGINE
import com.femastudios.esr.availablity.AvailabilityChecker
import com.femastudios.esr.availablity.AvailabilityState
import com.femastudios.esr.availablity.GroupAvailability
import org.apache.commons.jexl3.JexlExpression
import java.time.Duration

interface Group<TEST : Test> : AvailabilityChecker<GroupAvailability> {
    val name : String
    val timeoutIn: Duration?
    val refreshEvery: Duration?
    val criticalWhen: JexlExpression
    val errorWhen: JexlExpression
    val warningWhen: JexlExpression
    val servers: List<Server>
    val tests: List<TEST>

    val serviceProvider: ServiceProvider


    companion object {
        val DEFAULT_CRITICAL_ERROR_CONDITION = JEXL_ENGINE.createExpression("available.count == 0 && warning.count == 0")
        val DEFAULT_ERROR_CONDITION = JEXL_ENGINE.createExpression("available.count == 0 && warning.count == 0")
        val DEFAULT_WARNING_CONDITION = JEXL_ENGINE.createExpression("not_available.count > 0")
    }
}