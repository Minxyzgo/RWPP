/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.Color
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Surface
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OffscreenComposeRenderer(val width: Int, val height: Int) : AutoCloseable {
    private val surface = Surface.makeRasterN32Premul(width, height)
    private val canvas = surface.canvas.asComposeCanvas()

    @OptIn(InternalComposeUiApi::class)
    private val scene = androidx.compose.ui.scene.PlatformLayersComposeScene(
        size = IntSize(width, height)
    )

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE)
    private val pixels = (image.raster.dataBuffer as DataBufferInt).data

    private val byteBuffer = ByteBuffer.allocateDirect(width * height * 4)
        .order(ByteOrder.nativeOrder())

    private val pixmap: Pixmap by lazy {
        val addr = UnsafeAccess.getBufferAddress(byteBuffer)
        Pixmap.make(
            info = ImageInfo.makeN32Premul(width, height),
            addr = addr,
            rowBytes = width * 4
        )
    }

    @OptIn(InternalComposeUiApi::class)
    fun setContent(content: @androidx.compose.runtime.Composable () -> Unit) {
        scene.setContent(content)
    }

    @OptIn(InternalComposeUiApi::class)
    fun render(): BufferedImage {
        surface.canvas.clear(Color.TRANSPARENT)

        val nanoTime = System.nanoTime()
        scene.render(canvas, nanoTime)
        surface.makeImageSnapshot().use { snapshot ->
            snapshot.readPixels(
                dst = pixmap,
                srcX = 0,
                srcY = 0,
                cache = false
            )
        }

        byteBuffer.rewind()
        byteBuffer.asIntBuffer().get(pixels)
        return image
    }

    @OptIn(InternalComposeUiApi::class)
    fun shouldUpdate() = scene.hasInvalidations()

    @OptIn(InternalComposeUiApi::class)
    override fun close() {
        surface.close()
        scene.close()
    }
}

object UnsafeAccess {
    private val unsafe: sun.misc.Unsafe = try {
        val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe")
        field.isAccessible = true
        field.get(null) as sun.misc.Unsafe
    } catch (e: Exception) {
        throw RuntimeException("Unable to get Unsafe instance.", e)
    }

    private val addressOffset: Long = try {
        val bufferClass = java.nio.Buffer::class.java
        unsafe.objectFieldOffset(bufferClass.getDeclaredField("address"))
    } catch (e: Exception) {
        throw RuntimeException("Unable to get address.", e)
    }

    fun getBufferAddress(buffer: ByteBuffer): Long {
        if (!buffer.isDirect) throw IllegalArgumentException("Need DirectByteBuffer.")
        return unsafe.getLong(buffer, addressOffset)
    }
}