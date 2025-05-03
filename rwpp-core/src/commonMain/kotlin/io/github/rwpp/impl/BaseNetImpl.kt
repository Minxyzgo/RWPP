/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import io.github.rwpp.net.BBSProtocol
import io.github.rwpp.net.Client
import io.github.rwpp.net.Net
import io.github.rwpp.net.Packet
import io.github.rwpp.net.RTSBoxProtocol
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.DataInputStream
import java.util.concurrent.TimeUnit

abstract class BaseNetImpl : Net {
    override val packetDecoders: MutableMap<Int, (DataInputStream) -> Packet> = mutableMapOf()
    override val listeners: MutableMap<Int, MutableList<(Client?, Packet) -> Boolean>> = mutableMapOf()
    override val client: OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(Interceptor { chain ->
            val request = chain.request()
            request.newBuilder()
                .removeHeader("Accept-Encoding")
                .build()
            chain.proceed(request)
        })
        .readTimeout(5000L, TimeUnit.MILLISECONDS)
        .build()

    override val bbsProtocols: MutableList<BBSProtocol> = mutableListOf(RTSBoxProtocol)
}