/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net

import com.eclipsesource.json.Json
import io.github.rwpp.core.Initialization
import io.github.rwpp.game.Game
import io.github.rwpp.game.GameRoom
import io.github.rwpp.io.GameInputStream
import io.github.rwpp.logger
import io.github.rwpp.net.packets.ServerPacket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import okhttp3.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.lang.Exception
import java.util.*
import kotlin.reflect.full.createInstance

interface Net : KoinComponent, Initialization {
    /**
     * The map of packet decoders, key is the packet type, value is a lambda that takes a DataInputStream and returns a Packet.
     */
    val packetDecoders: MutableMap<Int, (DataInputStream) -> Packet>

    /**
     * listeners for each packet type, key is the packet type, value is a list of lambdas that take a Client and a Packet and return a Boolean.
     *
     * If the lambda returns true, then the packet would not be resolved by the game.
     */
    val listeners: MutableMap<Int, MutableList<(Client?, Packet) -> Boolean>>

    /**
     * Protocols for search mods.
     */
    val bbsProtocols: MutableList<BBSProtocol>

    /**
     * Default Okhttp3 client.
     */
    val client: OkHttpClient

    /**
     * Send a packet to the server.
     */
    fun sendPacketToServer(packet: Packet)

    /**
     * Send a packet to all clients. (if host)
     * @see [GameRoom.isHost]
     */
    fun sendPacketToClients(packet: Packet)

    fun openUriInBrowser(uri: String)

    override fun init() {
        registerListeners()
    }

    fun getLatestVersionProfile(): LatestVersionProfile? {
        return runCatching {
            val locale = Locale.getDefault()
            val request = Request.Builder().url(
                "https://api.github.com/repos/Minxyzgo/RWPP/releases/latest"
            ).build()
            val response = client.newCall(request).execute()

            response.body?.string()?.let { str ->
                val version = Json.parse(str).asObject().getString("tag_name", "null")
                val body = Json.parse(str).asObject().getString("body", "null")
                val prerelease = Json.parse(str).asObject().getBoolean("prerelease", false)
                LatestVersionProfile(version, body, prerelease)
            }
        }.getOrNull()
    }

    fun searchBBS(
        protocol: BBSProtocol,
        page: Int,
        keyword: String,
        type: ResourceType,
        callback: (Result<Array<NetResourceInfo>>) -> Unit
    ) {
        // 构建请求对象
        val request = Request.Builder()
            .url(protocol.url)
            .post(protocol.requestBodyProvider(page, keyword, type))
            .build()

        // 异步执行请求
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                val result = if (response.isSuccessful) {
                    protocol.resultParser(response)?.let {
                        Result.success(it)
                    } ?: Result.failure(IOException("Unknown response body"))
                } else {
                    Result.failure(IOException("HTTP error code: ${response.code}"))
                }
                callback(result)
            }
        })
    }

    fun getBytes(url: String): ByteArray? {
        val request = Request.Builder()
            .url(url)
            .build()
        return client.newCall(request).execute().body?.byteStream()?.readBytes()
    }

    fun downloadFile(
        url: String,
        outputFile: File,
        progressCallback: (progress: Float) -> Unit
    ) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                progressCallback(-1f)

                logger.error(e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }

                var downloadedBytes: Long = 0
                val body = response.body ?: return
                val contentLength = body.contentLength()
                var inputStream: InputStream? = null
                var outputStream: RandomAccessFile? = null

                try {
                    inputStream = body.byteStream()
                    outputStream = RandomAccessFile(outputFile, "rw")

                    val buffer = ByteArray(4096)
                    var read: Int

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        downloadedBytes += read.toLong()
                        progressCallback((downloadedBytes.toFloat() / contentLength.toFloat()).apply {
                            logger.info("downloading: $this")
                        })
                    }

                    logger.info("Download completed: ${outputFile.absoluteFile}")
                } catch (e: Exception) {
                    logger.error(e.stackTraceToString())
                    progressCallback(-1f)
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            }
        })
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun CoroutineScope.getRoomListFromSourceUrl(url: List<String>): List<RoomDescription> =
        withContext(Dispatchers.IO) {
            withTimeout(5000L) {
                val channel = Channel<Response>()

                url.map {
                    val request = Request.Builder()
                        .url(it)
                        .addHeader("User-Agent", "rw android 176 zh")
                        .addHeader("Language", "zh")
                        .get()
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful && channel.isEmpty) {
                                channel.trySend(response)
                            } else response.close()
                        }
                    })
                }

                val result = channel.receive()

                val roomDescriptions = mutableListOf<RoomDescription>()

                result.body!!.source().use {
                    val read = it.readUtf8Line()
                    if (read?.contains("CORRODINGGAMES") != true) throw RuntimeException("Unknown header: $read")
                    while (true) {
                        val r = it.readUtf8Line()?.split(",") ?: break
                        try {
                            val desc = RoomDescription(
                                r[0],
                                r[1],
                                r[2].toInt(),
                                r[3],
                                r[4],
                                r[5].toLong(),
                                r[6].toBooleanStrict(),
                                r[7],
                                r[8].toBooleanStrict(),
                                r[9],
                                r[10],
                                r[11],
                                r[12],
                                r[13].toBooleanStrict(),
                                r[14],
                                r[15].toIntOrNull(),
                                r[16].toIntOrNull(),
                                r[17].toBooleanStrict(),
                                r[18].ifBlank { r[0] },
                                r[19].toBooleanStrict(),
                                r[20],
                                r[21].toInt(),
                            )

                            roomDescriptions.add(desc)
                        } catch (e: Throwable) {
                            throw RuntimeException("Parse error when reading: ${r.joinToString(",")}", e)
                        }
                    }
                }

                result.close()
                channel.close()

                roomDescriptions
            }
        }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Packet> Net.registerPacketListener(
    packetType: Int,
    noinline listener: (Client?, T) -> Boolean
) {
    val method = T::class.java.getDeclaredMethod("readPacket", GameInputStream::class.java)
    packetDecoders[packetType] =
        {
            val p = T::class.createInstance()
            method.invoke(p, GameInputStream(it))
            p
        }
    listeners.getOrPut(packetType) { mutableListOf() }.add(listener as (Client?, Packet) -> Boolean)
}

fun Net.registerListeners() {
    val game = get<Game>()
    registerPacketListener<ServerPacket.ServerInfoGetPacket>(
        InternalPacketType.PRE_GET_SERVER_INFO_FROM_LIST.type
    ) { client, _ ->
        val room = game.gameRoom
        client?.sendPacketToClient(
            ServerPacket.ServerInfoReceivePacket(
                room.localPlayer.name + "'s game",
                room.getPlayers().size,
                room.maxPlayerCount,
                room.selectedMap.mapName,
                "",
                "v1.15 - RWPP Client",
                room.mods.joinToString(", "),
                if (room.isStartGame) ServerStatus.InGame else ServerStatus.BattleRoom
            )
        )

        true
    }
}