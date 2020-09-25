package com.femastudios.esr.util.decoders

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import java.time.DateTimeException
import java.time.ZoneId
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object ZoneIdDecoder : NullHandlingDecoder<ZoneId> {

    override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<ZoneId> {
        return if (node is StringNode) {
            try {
                ZoneId.of(node.value).valid()
            } catch (e: DateTimeException) {
                ThrowableFailure(e).invalid()
            }
        } else ConfigFailure.DecodeError(node, type).invalid()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun supports(type: KType): Boolean {
        return type == typeOf<ZoneId>()
    }
}