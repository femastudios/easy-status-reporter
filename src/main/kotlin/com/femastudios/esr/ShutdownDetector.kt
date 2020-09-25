package com.femastudios.esr

import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class ShutdownDetector {
    private val onShutdownAnomalyListeners = HashSet<OnShutdownAnomaly>()

    fun addOnShutdownAnomalyListener(listener: OnShutdownAnomaly) {
        onShutdownAnomalyListeners.add(listener)
    }

    private val registered = AtomicBoolean(false)

    fun register() {
        if (!registered.getAndSet(true)) {
            val f = try {
                FileChannel.open(
                    INSTANCE_INFO_FILE.toPath(),
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE
                )
            } catch (e: IOException) {
                logger.error(e) { "Unable to open and lock instance file for read/write. Is another instance already running?" }
                exitProcess(1)
            }

            val lastShutdownAnomaly = f.readInstant()
            if (lastShutdownAnomaly != null) {
                onShutdownAnomalyListeners.forEach { it.onShutdownAnomaly(lastShutdownAnomaly) }
            }

            val thread = thread(isDaemon = true) {
                while (true) {
                    try {
                        val instant = Instant.now()
                        logger.debug { "Write instance file at %s".format(instant.toString()) }
                        f.writeInstant(instant)
                    } catch (e: IOException) {
                        logger.error(e) { "Unable to write instance file" }
                    }
                    try {
                        Thread.sleep(10000)
                    } catch (ignored: InterruptedException) {
                        break
                    }
                }
            }

            Runtime.getRuntime().addShutdownHook(Thread {
                logger.info { "Shutdown requested" }
                thread.interrupt()
                thread.join()
                try {
                    f.close()
                    Files.delete(INSTANCE_INFO_FILE.toPath())
                } catch (e: IOException) {
                    logger.error(e) { "Unable to delete instance info file" }
                }
            })
        } else error("Already registered")
    }

    @Throws(IOException::class)
    private fun FileChannel.readInstant(): Instant? {
        position(0)
        val bb = ByteBuffer.allocate(50)
        val read = try {
            read(bb)
        } catch (e: IOException) {
            logger.error(e) { "Unable to read instance info file" }
            0
        }
        if (read <= 0) return null

        val string = StandardCharsets.UTF_8.decode(bb.slice(0, read))
        return try {
            Instant.parse(string)
        } catch (e: FileNotFoundException) {
            null
        } catch (e: DateTimeParseException) {
            logger.error(e) { "Instance info file contained invalid data" }
            null
        }
    }

    @Throws(IOException::class)
    private fun FileChannel.writeInstant(instant: Instant) {
        position(0)
        write(ByteBuffer.wrap(instant.toString().toByteArray()))
        truncate(position())
        force(true)
    }

    interface OnShutdownAnomaly {
        fun onShutdownAnomaly(estimateCrashTime: Instant)
    }

    companion object {
        private val INSTANCE_INFO_FILE = File("esr.instance-info")
    }
}