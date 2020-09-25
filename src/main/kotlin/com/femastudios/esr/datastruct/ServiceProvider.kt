package com.femastudios.esr.datastruct

class ServiceProvider(
    val group: Group<*>
) {
    private val map = mutableMapOf<Pair<Server, Test>, Service>()

    fun getService(server: Server, test: Test): Service {
        return map.getOrPut(server to test) {
            Service(group, server, test)
        }
    }
}