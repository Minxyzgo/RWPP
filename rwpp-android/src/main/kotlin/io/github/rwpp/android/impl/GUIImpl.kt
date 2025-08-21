/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.view.Window
import androidx.activity.ComponentDialog
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.corrodinggames.rts.gameFramework.j.ae
import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.ui.GUI
import io.github.rwpp.inject.SetInterfaceOn
import io.github.rwpp.scripts.Render
import io.github.rwpp.ui.Widget
import io.github.rwpp.widget.BorderCard
import io.github.rwpp.widget.RWPPTheme

@SetInterfaceOn([com.corrodinggames.rts.gameFramework.f.i::class])
interface GUIImpl : GUI {
    val self: com.corrodinggames.rts.gameFramework.f.i

    override var textPaint: GamePaint
        get() = GamePaintImpl(self.aC)
        set(value) {
            self.aC = (value as GamePaintImpl).paint
        }

    override fun showWidgetInGame(widget: (dispose: () -> Unit) -> Widget) {
        lateinit var dialog: ComponentDialog
        val composeView = ComposeView(CustomInGameActivity.instance!!)
            .apply {
                setContent {
                    RWPPTheme {
                        BorderCard(
                            modifier = Modifier
                                .wrapContentSize()
                        ) {
                            widget {
                                dialog.hide()
                            }.Render()
                        }
                    }
                }
            }
        dialog = ComponentDialog(CustomInGameActivity.instance!!).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(composeView)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }

    override fun showChatMessage(sender: String, message: String) {
        ae.a(sender, message)
    }
}