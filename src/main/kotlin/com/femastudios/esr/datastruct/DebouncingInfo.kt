package com.femastudios.esr.datastruct

import com.femastudios.debouncerthread.DebouncerThread
import java.time.Duration

data class DebouncingInfo(
    val waitTime: Duration,
    val maxWaitTime: Duration?
) {
    fun <T> newDebouncerThread(operation: (List<T>) -> Unit): DebouncerThread<T> {
        return DebouncerThread(
            waitTime = waitTime.toMillis(),
            maxWaitTime = maxWaitTime?.toMillis(),
            operation = operation
        )
    }

    companion object {
        val DEFAULT = DebouncingInfo(Duration.ofSeconds(30), Duration.ofMinutes(5))
    }
}