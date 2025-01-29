/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

fun parseColorToArgb(color: String): Int {
    // 移除字符串前的 # 号
    val cleanColor = color.replace("#", "")

    // 根据字符串长度判断是 #AARRGGBB 还是 #RRGGBB
    return when (cleanColor.length) {
        6 -> { // #RRGGBB 格式
            // 前面添加 AA（不透明度）部分
            val alpha = "FF"
            Integer.parseInt(alpha + cleanColor, 16)
        }
        8 -> { // #AARRGGBB 格式
            Integer.parseInt(cleanColor, 16)
        }
        else -> throw IllegalArgumentException("Invalid color format")
    }
}
