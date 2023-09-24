/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.selects.select
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

interface Net {

    val client: OkHttpClient
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun CoroutineScope.getRoomListFromSourceUrl(url: List<String>): List<RoomDescription> = withContext(Dispatchers.IO) {
        val c =  Channel<Response>(UNLIMITED)
        val job = async {
            val result = select {
                onTimeout(5000) {
                    throw RuntimeException("Response time out")
                }

                url.map {
                    println(it)
                    val request = Request.Builder()
                        .url(it)
                        .addHeader("User-Agent", "rw android 176 zh")
                        .addHeader("Language", "zh")
                        .get()
                        .build()
                    async(Dispatchers.IO) {
                        val s = client.newCall(request).execute()
                        c.send(s)
                        if(s.isSuccessful && s.body != null) s else { cancel() }
                    }
                }.forEach {
                    it.onAwait { result ->
                        result as Response
                    }
                }
            }

            val roomDescriptions = mutableListOf<RoomDescription>()

            result.body!!.source().use {
                val read = it.readUtf8Line()
                if(read?.contains("CORRODINGGAMES") != true) throw RuntimeException("Unknown header: $read")
                while(true) {
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
                    } catch(e: Throwable) {
                        throw RuntimeException("Parse error when reading: ${r.joinToString(",")}", e)
                    }
                }
            }

            roomDescriptions
        }

        val result = job.await()

        for(i in url.indices) {
            val res = c.receive()
            res.body?.close()
        }

        result
    }
}