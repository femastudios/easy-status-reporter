package com.femastudios.esr.datastruct

import com.femastudios.esr.availablity.ServiceAvailability
import org.apache.commons.jexl3.JexlExpression

interface Test {

    val name: String?
    val displayName: String
    val criticalWhen: JexlExpression
    val errorWhen: JexlExpression
    val warningWhen: JexlExpression


    fun check(global: Global, service: Service): ServiceAvailability
}