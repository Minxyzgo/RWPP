/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.net

import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import com.eclipsesource.json.Json
import io.github.rwpp.appKoin
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import java.nio.charset.Charset

enum class ResourceType {
    Mod, Map
}

class NetResourceInfo(
    val id: String,
    val title: String,
    val description: String? = null,
    val bbsUrl: String? = null,
    val downloadUrl: String? = null,
    val version: String? = null,
    val author: String? = null,
    val imagePainter: Painter? = null
)

class BBSProtocol(
    val url: String,
    val name: String,
    val requestBodyProvider: (page: Int, keyword: String, type: ResourceType) -> RequestBody,
    val resultParser: (Response) -> Array<NetResourceInfo>?
)

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
            val net = appKoin.get<Net>()
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
                            imagePainter = info.getString("img", "").let { img ->
                                if (img.isNotBlank()) net.getBytes(img)?.let {
                                    BitmapPainter(it.decodeToImageBitmap())
                                } else null
                            }
                        )
                    )

                    id++
                }
            }.toTypedArray()
        }.getOrNull()
    }
)