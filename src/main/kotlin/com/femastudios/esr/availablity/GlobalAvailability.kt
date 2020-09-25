package com.femastudios.esr.availablity

import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.datastruct.Group
import java.util.*

class GlobalAvailability(
    val global: Global,
    groupAvailabilities: LinkedHashSet<GroupAvailability>
) : MultiAvailabilityHolder<Group<*>, GroupAvailability>(groupAvailabilities.associateByTo(LinkedHashMap()) { it.group })