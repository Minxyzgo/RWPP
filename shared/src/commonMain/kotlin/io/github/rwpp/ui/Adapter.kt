/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowManager {
    Small, Middle, Large
}

@Composable
fun ConstraintWindowManager(width: Dp, height: Dp): WindowManager {
    return when {
        width >= 1680.dp && height >= 900.dp -> WindowManager.Large
        width >= 1280.dp && height >= 720.dp -> WindowManager.Middle
        else -> WindowManager.Small
    }
}