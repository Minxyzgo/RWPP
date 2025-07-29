/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.external

/**
 * Abstract class for launching external jar extensions.
 */
abstract class ExtensionLauncher {
    /**
     * Initialize the extension.
     */
    abstract fun init()

    /**
     * Called when the game is loaded.
     */
    abstract fun onLoaded()
}