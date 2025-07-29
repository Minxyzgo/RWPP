/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.coil

import coil3.ImageLoader
import coil3.fetch.Fetcher
import coil3.request.Options
import io.github.rwpp.ui.Imageable

class ImageableFetcherFactory : Fetcher.Factory<Imageable> {
    override fun create(
        data: Imageable,
        options: Options,
        imageLoader: ImageLoader
    ): Fetcher? {
        return ImageableFetcher(data)
    }
}
