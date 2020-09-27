package com.femastudios.esr.datastruct

import com.femastudios.debouncerthread.DebouncerThread
import java.time.Duration

data class DebouncingInfo(
    val waitTime: Duration = Duration.ofSeconds(30),
    val maxWaitTime: Duration? = Duration.ofMinutes(5)
) {

    fun <T> newDebouncerThread(name : String, operation: (List<T>) -> Unit): DebouncerThread<T> {
        return DebouncerThread(
            waitTime = waitTime.toMillis(),
            maxWaitTime = maxWaitTime?.toMillis(),
            operation = operation,
            name = name
        )
    }
}