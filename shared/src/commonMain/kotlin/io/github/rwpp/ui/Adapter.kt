/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.rwpp.LocalWindowManager

enum class WindowManager {
    Small, Middle, Large
}

@Composable
fun GeneralProportion(): Float {
    val manager = LocalWindowManager.current
    return when(manager) {
        WindowManager.Small -> 0.8f
        WindowManager.Middle -> 0.65f
        WindowManager.Large -> 0.5f
    }
}

@Composable
fun LargeProportion(): Float {
    val manager = LocalWindowManager.current
    return when(manager) {
        WindowManager.Small -> 0.95f
        WindowManager.Middle -> 0.8f
        WindowManager.Large -> 0.65f
    }
}

@Composable
fun scaleFitFloat(): Float {
    val manager = LocalWindowManager.current
    return when(manager) {
        WindowManager.Small -> 0.7f
        WindowManager.Middle -> 0.8f
        WindowManager.Large -> 1f
    }
}

@Composable
fun Modifier.scaleFit(): Modifier {
    val manager = LocalWindowManager.current
    return this.scale(scaleFitFloat())
}

@Composable
fun ConstraintWindowManager(width: Dp, height: Dp): WindowManager {
    return when {
        width >= 1680.dp && height >= 900.dp -> WindowManager.Large
        width >= 700.dp && height >= 500.dp -> WindowManager.Middle
        else -> WindowManager.Small
    }
}