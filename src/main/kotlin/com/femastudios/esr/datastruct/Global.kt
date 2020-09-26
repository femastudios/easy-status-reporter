package com.femastudios.esr.datastruct

import com.femastudios.esr.availablity.BaseAvailabilityChecker
import com.femastudios.esr.availablity.GlobalAvailability
import com.femastudios.esr.util.decoders.*
import com.sksamuel.hoplite.ConfigLoader
import java.io.File
import java.time.Duration
import java.time.ZoneId

data class Global(
    val webServer: WebServerConfig,
    val logLevel: LogLevel = LogLevel.INFO,
    val timezone: ZoneId = ZoneId.systemDefault(),
    val debounce: DebouncingInfo = DebouncingInfo(),
    val refreshEvery: Duration = Duration.ofSeconds(5),
    val timeoutIn: Duration = Duration.ofSeconds(5),
    val users: Set<User>,
    val agents: List<Agent> = emptyList(),
    val groups: List<Group<*>> = emptyList()
) : BaseAvailabilityChecker<GlobalAvailability>() {

    companion object {
        fun createConfigBuilder(): ConfigLoader.Builder = ConfigLoader.Builder()
            .addDecoder(UsersDecoder)
            .addDecoder(AgentDecoder)
            .addDecoder(GroupDecoder)
            .addDecoder(ServerDecoder)
            .addDecoder(IgnoreCaseEnumDecoder)
            .addDecoder(ZoneIdDecoder)
            .addDecoder(JexlExpressionDecoder)

        fun load(file: File): Global {
            return createConfigBuilder().build().loadConfigOrThrow(file)
        }
    }

    override fun checkAvailability(global: Global): GlobalAvailability {
        throw UnsupportedOperationException("Global availability should be computed by GlobalAvailabilityComputer")
    }

    public override fun registerNewAvailability(availability: GlobalAvailability) {
        super.registerNewAvailability(availability)
    }

    fun getBelongingGroup(server: Server): Group<*> {
        return groups.single { group ->
            group.servers.any { it === server }
        }
    }

    inline fun <reified G : Group<*>> getBelongingGroup(test: Test): G {
        return groups.single { group ->
            group.tests.any { it === test }
        } as G
    }

    fun isUserAllowed(username: String, password: String): Boolean {
        return users.any { it.username == username && it.password == password }
    }
}