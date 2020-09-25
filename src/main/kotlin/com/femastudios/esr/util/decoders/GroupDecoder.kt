package com.femastudios.esr.util.decoders

import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.datastruct.Group
import com.femastudios.esr.util.NodeConfigSource
import com.femastudios.esr.datastruct.groups.HttpGroup
import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType
import kotlin.reflect.typeOf


object GroupDecoder : Decoder<Group<*>> {

    override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<Group<*>> {
        if (node !is MapNode) {
            return ConfigFailure.Generic("group must be a map").invalid()
        }
        if (!node.hasKeyAt("type")) {
            return ConfigFailure.Generic("group must have a type").invalid()
        }

        val repType = (node["type"] as? StringNode)?.value
            ?: return ConfigFailure.Generic("group type must be a string").invalid()

        val loader = Global.createConfigBuilder().addPropertySource(NodeConfigSource(node))

        return when (repType) {
            "http" -> loader.addDecoder(HttpTestDecoder).build().loadConfigOrThrow<HttpGroup>().valid()
            else -> return ConfigFailure.Generic("Reporter type '$repType' unknown").invalid()
        }

    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun supports(type: KType): Boolean {
        return type == typeOf<Group<*>>()
    }
}