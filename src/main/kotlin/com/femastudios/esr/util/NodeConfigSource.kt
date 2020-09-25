package com.femastudios.esr.util

import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.fp.valid

class NodeConfigSource(val node: Node) : PropertySource {
    override fun node() = node.valid()
}