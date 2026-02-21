/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.desktop
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeCanvas
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.Color
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Surface
import org.newdawn.slick.Input
import org.newdawn.slick.InputListener
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OffscreenComposeRenderer(
    val width: Int,
    val height: Int,
    private val input: Input
) : AutoCloseable, InputListener {
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

    private var windowWidth = width
    private var windowHeight = height

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
    fun updateWindowSize(width: Int, height: Int) {
        windowWidth = width
        windowHeight = height
    }

    @OptIn(InternalComposeUiApi::class)
    override fun close() {
        surface.close()
        scene.close()
    }

    private fun calculateLogicalOffset(x: Float, y: Float): Offset {
//        val scaleX = windowWidth.toFloat() / width
//        val scaleY = windowHeight.toFloat() / height
//        val logicalX = x * scaleX
//        val logicalY = y * scaleY
        return Offset(x, y)
    }

    override fun mouseMoved(oldx: Int, oldy: Int, newx: Int, newy: Int) {
        sendPointerEvent(PointerEventType.Move, newx.toFloat(), newy.toFloat())
    }

    override fun mouseDragged(oldx: Int, oldy: Int, newx: Int, newy: Int) {
        val button = when {
            input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON) -> Input.MOUSE_LEFT_BUTTON
            input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON) -> Input.MOUSE_RIGHT_BUTTON
            else -> -1
        }

        sendPointerEvent(
            type = PointerEventType.Move,
            x = newx.toFloat(),
            y = newy.toFloat(),
            slickButton = button
        )
    }

    override fun mousePressed(button: Int, x: Int, y: Int) {
        sendPointerEvent(PointerEventType.Press, x.toFloat(), y.toFloat(), button)
    }

    override fun mouseReleased(button: Int, x: Int, y: Int) {
        sendPointerEvent(PointerEventType.Release, x.toFloat(), y.toFloat(), button)
    }

    private var lastX = 0
    private var lastY = 0

    override fun mouseWheelMoved(change: Int) {
        // 注意：Input 对象不直接传坐标给 wheel，我们需要保存最后的鼠标位置
        // 或者在 update 轮询中处理。这里假设通过 Input.getMouseX/Y 获取
        // change > 0 是向上滚动，Compose 需要负的 delta
        sendPointerEvent(
            PointerEventType.Scroll,
            lastX.toFloat(), lastY.toFloat(),
            scrollDelta = Offset(0f, -change.toFloat())
        )
    }

    override fun mouseClicked(p0: Int, p1: Int, p2: Int, p3: Int) {
        // do nothing
    }

    @OptIn(InternalComposeUiApi::class)
    private fun sendPointerEvent(
        type: PointerEventType,
        x: Float,
        y: Float,
        slickButton: Int = -1,
        scrollDelta: Offset = Offset.Zero
    ) {
        lastX = x.toInt()
        lastY = y.toInt()

        val button = when (slickButton) {
            Input.MOUSE_LEFT_BUTTON -> PointerButton.Primary
            Input.MOUSE_RIGHT_BUTTON -> PointerButton.Secondary
            Input.MOUSE_MIDDLE_BUTTON -> PointerButton.Tertiary
            else -> null
        }

        scene.sendPointerEvent(
            eventType = type,
            position = calculateLogicalOffset(x, y),
            scrollDelta = scrollDelta,
            button = button,
            timeMillis = System.currentTimeMillis()
        )
    }


    @OptIn(InternalComposeUiApi::class)
    override fun keyPressed(key: Int, c: Char) {
        val composeKey = slickToComposeKey(key)

        val event = createKeyEvent(
            type = KeyEventType.KeyDown,
            key = composeKey,
            codePoint = c.code
        )

        scene.sendKeyEvent(event)
    }

    @OptIn(InternalComposeUiApi::class)
    override fun keyReleased(key: Int, c: Char) {
        val composeKey = slickToComposeKey(key)

        val event = createKeyEvent(
            type = KeyEventType.KeyUp,
            key = composeKey,
            codePoint = c.code
        )

        scene.sendKeyEvent(event)
    }

    @OptIn(InternalComposeUiApi::class)
    private fun createKeyEvent(type: KeyEventType, key: Key, codePoint: Int): KeyEvent {
        val isCtrlPressed = input.isKeyPressed(Input.KEY_LCONTROL)
        val isAltPressed = input.isKeyPressed(Input.KEY_LALT)
        val isShiftPressed = input.isKeyPressed(Input.KEY_LSHIFT)
        return KeyEvent(
            key = key,
            type = type,
            codePoint = codePoint,
            isCtrlPressed = isCtrlPressed,
            isAltPressed = isAltPressed,
            isShiftPressed = isShiftPressed
        )
    }

    private fun slickToComposeKey(slickKey: Int): Key {
        return when (slickKey) {
            Input.KEY_A -> Key.A
            Input.KEY_B -> Key.B
            Input.KEY_C -> Key.C
            Input.KEY_D -> Key.D
            Input.KEY_E -> Key.E
            Input.KEY_F -> Key.F
            Input.KEY_G -> Key.G
            Input.KEY_H -> Key.H
            Input.KEY_I -> Key.I
            Input.KEY_J -> Key.J
            Input.KEY_K -> Key.K
            Input.KEY_L -> Key.L
            Input.KEY_M -> Key.M
            Input.KEY_N -> Key.N
            Input.KEY_O -> Key.O
            Input.KEY_P -> Key.P
            Input.KEY_Q -> Key.Q
            Input.KEY_R -> Key.R
            Input.KEY_S -> Key.S
            Input.KEY_T -> Key.T
            Input.KEY_U -> Key.U
            Input.KEY_V -> Key.V
            Input.KEY_W -> Key.W
            Input.KEY_X -> Key.X
            Input.KEY_Y -> Key.Y
            Input.KEY_Z -> Key.Z
            Input.KEY_0 -> Key.Zero
            Input.KEY_1 -> Key.One
            Input.KEY_2 -> Key.Two
            Input.KEY_3 -> Key.Three
            Input.KEY_4 -> Key.Four
            Input.KEY_5 -> Key.Five
            Input.KEY_6 -> Key.Six
            Input.KEY_7 -> Key.Seven
            Input.KEY_8 -> Key.Eight
            Input.KEY_9 -> Key.Nine
            Input.KEY_ENTER -> Key.Enter
            Input.KEY_ESCAPE -> Key.Escape
            Input.KEY_BACK -> Key.Backspace
            Input.KEY_TAB -> Key.Tab
            Input.KEY_SPACE -> Key.Spacebar
            Input.KEY_MINUS -> Key.Minus
            Input.KEY_EQUALS -> Key.Equals
            Input.KEY_LEFT -> Key.DirectionLeft
            Input.KEY_RIGHT -> Key.DirectionRight
            Input.KEY_UP -> Key.DirectionUp
            Input.KEY_DOWN -> Key.DirectionDown
            Input.KEY_LSHIFT -> Key.ShiftLeft
            Input.KEY_RSHIFT -> Key.ShiftRight
            Input.KEY_LCONTROL -> Key.CtrlLeft
            Input.KEY_RCONTROL -> Key.CtrlRight
            Input.KEY_LALT -> Key.AltLeft
            Input.KEY_RALT -> Key.AltRight
            Input.KEY_COMMA -> Key.Comma
            Input.KEY_PERIOD -> Key.Period
            Input.KEY_SLASH -> Key.Slash
            Input.KEY_SEMICOLON -> Key.Semicolon
            Input.KEY_APOSTROPHE -> Key.Apostrophe
            Input.KEY_BACKSLASH -> Key.Backslash
            Input.KEY_GRAVE -> Key.Grave
            Input.KEY_F1 -> Key.F1
            Input.KEY_F2 -> Key.F2
            Input.KEY_F3 -> Key.F3
            Input.KEY_F4 -> Key.F4
            Input.KEY_F5 -> Key.F5
            Input.KEY_F6 -> Key.F6
            Input.KEY_F7 -> Key.F7
            Input.KEY_F8 -> Key.F8
            Input.KEY_F9 -> Key.F9
            Input.KEY_F10 -> Key.F10
            Input.KEY_F11 -> Key.F11
            Input.KEY_F12 -> Key.F12
            else -> Key.Unknown
        }
    }

    override fun inputEnded() {}
    override fun inputStarted() {}
    override fun isAcceptingInput(): Boolean = true
    override fun setInput(input: Input?) {}
    override fun controllerButtonPressed(controller: Int, button: Int) {}
    override fun controllerButtonReleased(controller: Int, button: Int) {}
    override fun controllerDownPressed(controller: Int) {}
    override fun controllerDownReleased(controller: Int) {}
    override fun controllerLeftPressed(controller: Int) {}
    override fun controllerLeftReleased(controller: Int) {}
    override fun controllerRightPressed(controller: Int) {}
    override fun controllerRightReleased(controller: Int) {}
    override fun controllerUpPressed(controller: Int) {}
    override fun controllerUpReleased(controller: Int) {}
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