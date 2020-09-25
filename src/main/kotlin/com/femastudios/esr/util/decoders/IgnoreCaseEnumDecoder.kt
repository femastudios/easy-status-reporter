package com.femastudios.esr.util.decoders

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KClass
import kotlin.reflect.KType

object IgnoreCaseEnumDecoder : NullHandlingDecoder<Any> {

    override fun supports(type: KType): Boolean =
        type.classifier is KClass<*> && (type.classifier as KClass<*>).java.isEnum

    override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<Any> {
        val klass = type.classifier as KClass<*>

        @Suppress("UNCHECKED_CAST")
        fun decode(value: String): ConfigResult<Any> {
            val t = klass.java.enumConstants.find { it.toString().equals(value, ignoreCase = true) }
            return t?.valid() ?: ConfigFailure.InvalidEnumConstant(node, type, value).invalid()
        }

        return when (node) {
            is StringNode -> decode(node.value)
            is BooleanNode -> decode(node.value.toString())
            is LongNode -> decode(node.value.toString())
            is DoubleNode -> decode(node.value.toString())
            else -> ConfigFailure.DecodeError(node, type).invalid()
        }
    }
}
