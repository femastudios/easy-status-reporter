package com.femastudios.esr.util

import com.femastudios.esr.Main
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.valid
import org.apache.commons.jexl3.*
import java.time.Duration
import java.util.*
import kotlin.math.absoluteValue

val JEXL_ENGINE = JexlBuilder().arithmetic(object : JexlArithmetic(true) {


}).create()!!

fun Map<String, *>.createJexlContext(): MapContext {
    return MapContext(
        this + mapOf(
            "always" to true,
            "never" to false
        )
    )
}

@Throws(InterruptedException::class)
fun Collection<Runnable>.executeParallely(threadName: String) {
    val threads = HashSet<Thread>(size)
    for (runnable in this) {
        val t = Thread(runnable, threadName)
        t.start()
        threads.add(t)
    }
    for (thread in threads) {
        thread.join()
    }
}

class JexlEvaluationException(override val message: String, cause: Throwable? = null) : Exception(message, cause)

fun JexlExpression.evaluateBoolean(context: JexlContext, name: String): Boolean {
    return try {
        val result = evaluate(context)
        if (result is Boolean) result
        else {
            throw JexlEvaluationException("$name expression must return a boolean value, " + result::class + " returned")
        }
    } catch (je: JexlException) {
        throw JexlEvaluationException("Error evaluating $name expression", je)
    }
}

fun Duration.format(): String {
    val seconds = seconds
    val absSeconds = seconds.absoluteValue
    val positive = String.format(
        "%dh %02dm %02ds",
        absSeconds / 3600,
        absSeconds % 3600 / 60,
        absSeconds % 60
    )
    return if (seconds < 0) "-$positive" else positive
}

inline fun <reified T> Validated<ConfigFailure, *>.cast(): Validated<ConfigFailure, T> {
    return when (this) {
        is Validated.Invalid -> this
        is Validated.Valid -> (value as T).valid()
    }
}

fun getBuildVersion(): String {
    return Main::class.java.getPackage().implementationVersion
}
