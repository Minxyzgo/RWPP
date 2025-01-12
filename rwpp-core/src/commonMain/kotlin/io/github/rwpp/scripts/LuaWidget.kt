/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.scripts

sealed class LuaWidget {
    class LuaText @JvmOverloads constructor(
        val text: String,
        val size: Int = 16,
        val color: LuaColor = LuaColor(0, 0, 0, 255),
    ) : LuaWidget()
    class LuaImage(val imageUrl: String) : LuaWidget()
    class LuaTextButton(val text: String, val onClick: () -> Unit) : LuaWidget()
    class LuaCheckbox(val text: String, val checked: Boolean, val onClick: String) : LuaWidget()
    class LuaSlider(val min: Int, val max: Int, val value: Int, val onChange: String) : LuaWidget()
    class LuaDropdown(val options: List<String>, val selected: Int, val onChange: String) : LuaWidget()
    class LuaInput(val placeholder: String, val value: String, val onChange: String) : LuaWidget()
}