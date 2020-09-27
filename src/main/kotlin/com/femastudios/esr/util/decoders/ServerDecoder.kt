package com.femastudios.esr.util.decoders

import com.femastudios.esr.datastruct.Server
import com.femastudios.esr.util.cast
import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.DataClassDecoder
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.net.InetAddress
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object ServerDecoder : Decoder<Server> {

    override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<Server> {
        @Suppress("UNCHECKED_CAST")
        return when (node) {
            is StringNode -> Server(address = InetAddress.getByName(node.value)).valid()
            is MapNode -> DataClassDecoder().decode(node, type, context).cast()
            else -> ConfigFailure.Generic("server must be a map or an address").invalid()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun supports(type: KType): Boolean {
        return type == typeOf<Server>()
    }
}