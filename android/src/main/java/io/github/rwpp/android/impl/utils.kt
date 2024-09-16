/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import io.github.rwpp.net.Packet
import io.github.rwpp.utils.io.GameOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File

fun Packet.asGamePacket(): com.corrodinggames.rts.gameFramework.j.bi {
    val byteArrayOutput = ByteArrayOutputStream()
    val gameOutput = GameOutputStream(
        DataOutputStream(byteArrayOutput)
    )
    gameOutput.use { writePacket(it) }
    val bytes = byteArrayOutput.toByteArray()
    byteArrayOutput.close()
    return com.corrodinggames.rts.gameFramework.j.bi(type.type).apply {
        c = bytes
    }
}

fun sendKickToClient(c: com.corrodinggames.rts.gameFramework.j.c, reason: String) {
    val t = GameEngine.t()
    t.bU::class.java.getDeclaredMethod("b", com.corrodinggames.rts.gameFramework.j.c::class.java, String::class.java).apply {
        isAccessible = true
    }.invoke(t.bU, c, reason)
}

fun Resources.getResourceFileName(i: Int): String {
    val value = TypedValue()
    getValue(i, value, true)
    return value.string.toString().removePrefix("res/")
}


fun copyAssets(context: Context, assetDir: String, targetDir: String) {
    if (assetDir.isEmpty() || targetDir.isEmpty()) {
        return
    }
    val separator = File.separator

    // 获取assets目录assetDir下一级所有文件及文件夹
    val fileNames = context.resources.assets.list(assetDir)!!
    // 如果是文件夹(目录),则继续递归遍历
    if (fileNames.isNotEmpty()) {
        val targetFile = File(targetDir)
        if (!targetFile.exists() && !targetFile.mkdirs()) {
            return
        }
        for (fileName in fileNames) {
            copyAssets(context, assetDir + separator + fileName, targetDir + separator + fileName)
        }
    } else { // 文件,则执行拷贝
        val fi = File(targetDir)
        fi.parentFile!!.run { if(!exists()) mkdirs() }
        if(!fi.exists()) fi.createNewFile()
        fi.writeBytes(context.assets.open(assetDir).use { it.readBytes() })
    }

}