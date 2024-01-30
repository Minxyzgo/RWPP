/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.corrodinggames.rts.appFramework.GameViewNonSurface

class RWPPGameViewNonSurface(var1: Context?, var2: AttributeSet?) : GameViewNonSurface(var1, var2) {
    override fun onDraw(p0: Canvas) {
        try {
            super.onDraw(p0)
        } catch(e: Exception) {
            // do nothing
        }
    }
}