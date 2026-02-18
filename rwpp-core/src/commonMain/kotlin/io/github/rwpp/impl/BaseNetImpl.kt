/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.impl

import com.eclipsesource.json.Json
import io.github.rwpp.appKoin
import io.github.rwpp.config.MultiplayerPreferences
import io.github.rwpp.config.ServerConfig
import io.github.rwpp.config.ServerType
import io.github.rwpp.net.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.get
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

    override val scope: CoroutineScope = CoroutineScope(SupervisorJob())
    override val bbsProtocols: MutableList<BBSProtocol> = mutableListOf(RTSBoxProtocol, RTSBoxDownloadWeeklyProtocol)
    override val roomListProvider: MutableMap<String, suspend () -> List<RoomDescription>> = mutableMapOf()
    override val roomListHostProtocol: MutableMap<String, (maxPlayer: Int, enableMods: Boolean, isPublic: Boolean) -> String> = mutableMapOf()

    override fun init() {
        val allServerConfig = get<MultiplayerPreferences>().allServerConfig
        if (allServerConfig.none { it.name == "Official Room List" }) {
            allServerConfig.add(officialRoomList)
        }
        if (allServerConfig.none { it.name == "Q Room List" }) {
            allServerConfig.add(qRoomList)
        }

        roomListHostProtocol["RCN"] = { maxPlayer, enableMods, isPublic ->
            if (enableMods) {
                if (isPublic) {
                    "Rmodupp$maxPlayer"
                } else {
                    "Rmodp$maxPlayer"
                }
            } else if (isPublic) {
                "Rnewupp$maxPlayer"
            } else {
                "Rnewp$maxPlayer"
            }
        }

        roomListHostProtocol["SCN"] = { maxPlayer, enableMods, isPublic ->
            if (enableMods) {
                if (isPublic) {
                    "Smodupp$maxPlayer"
                } else {
                    "Smodp$maxPlayer"
                }
            } else if (isPublic) {
                "Snewupp$maxPlayer"
            } else {
                "Snewp$maxPlayer"
            }
        }

        roomListHostProtocol["QN"] = { maxPlayer, enableMods, _ ->
            if (enableMods) {
                "Qmodp$maxPlayer"
            } else {
                "Qnewp$maxPlayer"
            }
        }

        roomListProvider["qRoomListProvider"] = {
            withContext(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("https://www.rtsbox.cn/api/paiwei/room_last.php")
                    .get()
                    .build()
                val net = appKoin.get<Net>()
                val response = net.client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val jsonBody = Json.parse(body)
                    val data = jsonBody.asObject().get("data").asArray()
                    buildList {
                        for (item in data) {
                            val info = item.asObject()
                            val roomId = info.getInt("internal_id", -1)
                            val mapName = info.getString("map_name", "???")
                            val currentPlayers = info.getInt("current_players", -1)
                            val maxPlayers = info.getInt("max_players", -1)
                            val isStarted = info.getInt("is_started", 0) == 1
                            val hostName = info.getString("host_name", "???")
                            add(RoomDescription(
                                roomId.toString(),
                                creator = hostName,
                                mapName = mapName,
                                playerCurrentCount = currentPlayers,
                                playerMaxCount = maxPlayers,
                                isOpen = true,
                                customIp = "Q$roomId",
                                status = if (isStarted) "ingame" else "battleroom",
                                version = "1.15-QN"
                            ))
                        }
                    }
                } else {
                    listOf()
                }
            }
        }
    }
}


val RTSBoxProtocol = BBSProtocol(
    "https://www.rtsbox.cn/api/search_bbs.php",
    "铁锈盒子",
    { page, keyword, type ->
        MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .apply {
                addFormDataPart("bbs_id", if (type == ResourceType.Mod) "4" else "5")
                addFormDataPart("keyword", keyword)
                addFormDataPart("page", page.toString())
            }
            .build()
    },
    { response ->
        runCatching {
            val jsonBody = Json.parse(response.body?.string()).asObject()
            var id = 0
            buildList {
                for (data in jsonBody.get("data").asArray()) {
                    val info = data.asObject()
                    val title = String(info.getString("title", "???").toByteArray(), Charsets.UTF_8)

                    add(
                        NetResourceInfo(
                            title + id, // ???
                            title,
                            bbsUrl = info.getString("bbsurl", "???"),
                            //downloadUrl = info.getString("downurl", "???"),
                            imageUrl = info.getString("img", null)
                        )
                    )

                    id++
                }
            }.toTypedArray()
        }.getOrNull()
    }
)

val RTSBoxDownloadWeeklyProtocol = BBSProtocol(
    "https://www.rtsbox.cn/api/lt_api/data.php",
    "铁锈盒子 下载周榜",
    { page, _, type ->
        MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .apply {
                addFormDataPart("catID", if (type == ResourceType.Mod) "mod" else "map")
                addFormDataPart("type", "WeekDownload")
                addFormDataPart("page", page.toString())
            }
            .build()
    },
    { response ->
        runCatching {
            val jsonBody = Json.parse(response.body?.string().apply { println(this) })
            buildList {
                for (data in jsonBody.asArray()) {
                    val info = data.asObject()
                    val title = String(info.getString("title", "???").toByteArray(), Charsets.UTF_8)
                    val postID = info.getInt("postID", -1)
                    val downloadNum = info.getInt("download_num", -1)

                    add(
                        NetResourceInfo(
                            postID.toString(),
                            title,
                            bbsUrl = "https://www.rtsbox.cn/$postID.html",
                            imageUrl = info.getString("img_url", null),
                            downloadNum = downloadNum
                        )
                    )
                }
            }.toTypedArray()
        }.getOrNull()
    }
)


val officialRoomList: ServerConfig
    get() = ServerConfig(
        "http://gs1.corrodinggames.com/masterserver/1.4/interface?action=list&game_version=176&game_version_beta=false;http://gs4.corrodinggames.net/masterserver/1.4/interface?action=list&game_version=176&game_version_beta=false",
        "Official Room List",
        ServerType.RoomList,
        editable = false,
        customRoomHostProtocol = "RCN"
    )
val qRoomList: ServerConfig
    get() = ServerConfig(
        "",
        "Q Room List",
        ServerType.RoomList,
        editable = false,
        customRoomListProvider = "qRoomListProvider",
        customRoomHostProtocol = "QN"
    )