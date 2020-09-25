package com.femastudios.esr.util.decoders

import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.datastruct.Agent
import com.femastudios.esr.util.NodeConfigSource
import com.femastudios.esr.datastruct.agents.TelegramAgent
import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object AgentDecoder : Decoder<Agent> {

    override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<Agent> {
        if (node !is MapNode) {
            return ConfigFailure.Generic("reporter must be a map").invalid()
        }
        if (!node.hasKeyAt("type")) {
            return ConfigFailure.Generic("Reporter must have a type").invalid()
        }

        val repType = (node["type"] as? StringNode)?.value
            ?: return ConfigFailure.Generic("Reporter type must be a string").invalid()

        val loader = Global.createConfigBuilder()
            .addPropertySource(NodeConfigSource(node))
            .build()

        return when (repType) {
            "telegram-bot" -> loader.loadConfigOrThrow<TelegramAgent>().valid()
            else -> return ConfigFailure.Generic("Reporter type '$repType' unknown").invalid()
        }

    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun supports(type: KType): Boolean {
        return type == typeOf<Agent>()
    }
}