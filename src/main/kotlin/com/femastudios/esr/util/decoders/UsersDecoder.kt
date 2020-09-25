package com.femastudios.esr.util.decoders

import com.femastudios.esr.datastruct.User
import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object UsersDecoder : Decoder<Set<User>> {

    override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<Set<User>> {
        if (node is MapNode) {
            return node.map.mapTo(HashSet()) { (username, passwordNode) ->
                if (passwordNode is StringNode) {
                    User(username, passwordNode.value)
                } else {
                    return ConfigFailure.Generic("password of user \"$username\" must be a string").invalid()
                }
            }.valid()
        } else {
            return ConfigFailure.DecodeError(node, type).invalid()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun supports(type: KType): Boolean {
        return type == typeOf<Set<User>>()
    }
}