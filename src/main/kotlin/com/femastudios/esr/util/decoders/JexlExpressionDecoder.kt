package com.femastudios.esr.util.decoders

import com.femastudios.esr.util.JEXL_ENGINE
import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import org.apache.commons.jexl3.JexlException
import org.apache.commons.jexl3.JexlExpression
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object JexlExpressionDecoder : NullHandlingDecoder<JexlExpression> {

    override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<JexlExpression> {
        return if (node is StringNode) {
            try {
                JEXL_ENGINE.createExpression(node.value).valid()
            } catch (e: JexlException) {
                ThrowableFailure(e).invalid()
            }
        } else ConfigFailure.DecodeError(node, type).invalid()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun supports(type: KType): Boolean {
        return type == typeOf<JexlExpression>()
    }
}