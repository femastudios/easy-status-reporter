package com.femastudios.esr.util.decoders

import com.femastudios.esr.datastruct.Global
import com.femastudios.esr.util.NodeConfigSource
import com.femastudios.esr.datastruct.tests.HttpTest
import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object HttpTestDecoder : Decoder<HttpTest> {

    override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<HttpTest> {
        return when(node) {
            is StringNode ->  HttpTest(path = node.value).valid()
            is MapNode -> Global.createConfigBuilder().addPropertySource(NodeConfigSource(node)).build().loadConfig()
            else -> ConfigFailure.Generic("group must be a map or a string").invalid()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun supports(type: KType): Boolean {
        return type == typeOf<HttpTest>()
    }
}