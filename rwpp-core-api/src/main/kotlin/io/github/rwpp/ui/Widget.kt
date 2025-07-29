/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

open class Widget {
    class Text @JvmOverloads constructor(
        val text: String,
        val size: Int = 16,
        val color: Color = Color(0),
        val isBold: Boolean = false,
    ) : Widget()
    class Image(val model: Any?) : Widget()
    class TextButton(val text: String, val onClick: () -> Unit) : Widget()
    class Checkbox(
        val text: String,
        val checked: () -> Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : Widget()
    class Slider(
        /** Range: from 0.0f to 1.0f */
        val value: Float,
        val onChange: (Float) -> Unit
    ) : Widget()
    class Dropdown(
        val options: Array<String>,
        val label: String,
        val defaultValue: () -> String,
        val onChange: (Int, String) -> Unit
    ) : Widget()
    class TextField(
        val label: String,
        val defaultText: () -> String,
        val onTextChanged: (String) -> Unit,
    ) : Widget()
    class Column(val widgets: List<Widget>) : Widget()
    class Row(val widgets: List<Widget>) : Widget()
}