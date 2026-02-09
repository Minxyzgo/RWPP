/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net.server

import com.sun.tools.javac.jvm.ByteCodes.ret
import java.lang.invoke.MethodHandles
import java.lang.reflect.Type
import java.util.*
import java.util.function.Consumer


/**
 * 用于重实现 Game Server Service
 *
 * @date 2025/10/18 10:41
 * @author Dr (dr@der.kim)
 */
abstract class NetService {
    fun resolveGameAddress(ip: String, forceTcp: Boolean): Triple<NetConnectType, NetConnectProtocolType, String> {
        val protocol = if (forceTcp) {
            NetConnectProtocolType.ForceTcp
        } else {
            NetConnectProtocolType.UNKNOWN
        }

        val connectionString = ip.trim()
        // ? 怎么框架他妈的没Log Service ?????
        //("Resolve IP from: $trimmed")

        var newIp =  when {
            connectionString.startsWith("get|") ->
                Triple(NetConnectType.Server, protocol, connectionString)
            // 前缀没想好
            //connectionString.startsWith("ws://") || onnectionString.startsWith("wss://") ->
            //    Triple(NetConnectType.WebSocket, protocol, connectionString)
            else ->
                // 不知道 那就全部塞给默认处理器
                Triple(NetConnectType.UNKNOWN, protocol, connectionString)
        }

        if (newIp.first == NetConnectType.UNKNOWN) {
            val rwTcpDomain = "_rw._tcp.$connectionString"
            val rwUdpDomain = "_rw._udp.$connectionString"

            // DOH
            // 妈的 怎么连HTTP都是到处拉屎
        }

        return newIp
    }

    enum class NetConnectType {
        Server,
        WebSocket,
        UNKNOWN,
        ;
    }

    enum class NetConnectProtocolType {
        TCP,
        ForceTcp,
        R_UDP,
        KCP,
        UNKNOWN,
        ;
    }
}