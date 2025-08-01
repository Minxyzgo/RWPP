/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import org.koin.core.component.KoinComponent

interface IUserInterface : KoinComponent {
    /**
     * Show a warning dialog to the user.
     */
    fun showWarning(reason: String, isKicked: Boolean = false)

    /**
     * Show a question dialog to the user.
     * @param callback The callback function to be called when the user submits the answer, or dismisses the dialog when given null.
     */
    fun showQuestion(title: String, message: String, callback: (String?) -> Unit)


    /**
     * Show a dialog to the user. Note that there can only be one dialog at a time.
     *
     * @param widget The widget to be displayed in the dialog.
     */
    fun showDialog(widget: Widget)
}