package com.femastudios.esr.datastruct

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL

data class WebServerConfig(
    val bind: InetAddress = InetAddress.getLoopbackAddress(),
    val port: Int = 7828,
    val title: String? = null,
    val url: URL
) {

    val socketAddress : InetSocketAddress
        get() = InetSocketAddress(bind, port)


}
