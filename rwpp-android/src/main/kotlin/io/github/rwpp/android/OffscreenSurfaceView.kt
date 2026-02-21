/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.graphics.ColorUtils
import io.github.rwpp.android.impl.GamePaintImpl
import io.github.rwpp.android.impl.OffscreenGameCanvasImpl
import io.github.rwpp.appKoin
import io.github.rwpp.game.Game
import io.github.rwpp.game.units.comp.EntityRangeUnitComp
import io.github.rwpp.game.units.comp.EntityRangeUnitComp.Companion.drawRange
import io.github.rwpp.game.world.World
import kotlin.concurrent.thread

class OffscreenSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var isRunning = false
    private var renderThread: Thread? = null

    private val renderer by lazy { RangeRenderer(width, height) }

    init {
        holder.addCallback(this)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSPARENT)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        renderThread = thread(start = true, name = "CircleRenderThread") {
            renderLoop()
        }
    }

    private fun renderLoop() {
        while (isRunning) {
            val canvas = holder.lockHardwareCanvas() ?: continue
            try {
              //  canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                renderer.render(canvas)
             //   Thread.sleep(16)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, he: Int) {
    }


    override fun surfaceDestroyed(h: SurfaceHolder) {
        isRunning = false
        renderThread?.join()
    }

    class RangeRenderer(val width: Int, val height: Int) {
        private val teamBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        private val teamCanvas = Canvas(teamBitmap)

        // 2. 总输出层：用于存放所有队伍叠加后的结果
        private val finalBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        private val finalCanvas = Canvas(finalBitmap)

        private var gameCanvas: OffscreenGameCanvasImpl = OffscreenGameCanvasImpl(teamCanvas)

        private val circlePaint = GamePaintImpl(Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            // 不需要特殊的 Xfermode，因为我们要的是覆盖效果
        })

        private val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // SRC_IN: 只有在 teamCanvas 有圆的地方才染上队色
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }

        fun render(mainCanvas: Canvas) = with(EntityRangeUnitComp) {
            // A. 先清空“最终成品图”
            finalCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            beforeDrawRange()

            layerGroups.forEach { (team, units) ->
                // B. 处理这一支队伍
                // 1. 清空当前队伍的临时层
                teamCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                // 2. 在临时层画圆
                teamCanvas.save()
                teamCanvas.scale(world.gameScale, world.gameScale)
                units.forEach { unit ->
                    gameCanvas.drawRange(unit, circlePaint, unit.maxAttackRange)
                }
                teamCanvas.restore()

                // 3. 给当前队伍上色 (此时 teamBitmap 里是这一队带透明度的圆)
                tintPaint.color = getTeamPaint(team).argb
                teamCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)

                // 4. 重要：把这一队的结果“合并”到总输出层
                // 使用默认混合模式 (SRC_OVER)，这样不同队伍的圈会叠在一起
                finalCanvas.drawBitmap(teamBitmap, 0f, 0f, null)
            }

            // C. 最后：一次性把所有队伍的结果画到主屏幕
            // 先清理主屏幕，再画成品
            mainCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            mainCanvas.drawBitmap(finalBitmap, 0f, 0f, null)
        }
    }
}