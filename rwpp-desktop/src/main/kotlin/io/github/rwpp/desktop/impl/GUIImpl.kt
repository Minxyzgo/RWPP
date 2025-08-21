/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop.impl

import android.graphics.Paint
import io.github.rwpp.desktop.inGameWidget
import io.github.rwpp.desktop.inGameWidgetDialog
import io.github.rwpp.desktop.resetInGameWidgetDialogLocation
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.ui.GUI
import io.github.rwpp.inject.SetInterfaceOn
import io.github.rwpp.ui.Widget

@SetInterfaceOn([com.corrodinggames.rts.gameFramework.f.g::class])
interface GUIImpl : GUI {
    val self: com.corrodinggames.rts.gameFramework.f.g

    override var textPaint: GamePaint
        get() = self.aC as GamePaint
        set(value) { self.aC = value as Paint }

    override fun showWidgetInGame(widget: (dispose: () -> Unit) -> Widget) {
        inGameWidget = widget { inGameWidgetDialog.isVisible = false }
        inGameWidgetDialog.pack()
        inGameWidgetDialog.isVisible = true
    }

    override fun showChatMessage(sender: String, message: String) {
        com.corrodinggames.rts.gameFramework.j.ad.a(sender, message)
    }
}