/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.coil

import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import io.github.rwpp.rwpp_core.generated.resources.Res
import io.github.rwpp.ui.Imageable
import okio.FileSystem
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream

class ImageableFetcher(
    private val data: Imageable
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        val source = ImageSource(
            data.openImageInputStream()?.source()?.buffer()
                ?: ByteArrayInputStream(Res.readBytes("drawable/error_missingmap.png")).source().buffer(),
            fileSystem = FileSystem.SYSTEM,
        )
        return SourceFetchResult(
            source = source,
            mimeType = null,
            dataSource = DataSource.DISK,
        )
    }
}