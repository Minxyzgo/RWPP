/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */
package io.github.rwpp.android

import com.corrodinggames.rts.appFramework.ep
import com.corrodinggames.rts.gameFramework.m.a
import com.corrodinggames.rts.gameFramework.m.l

interface IView : com.corrodinggames.rts.appFramework.f {
    fun drawCompleted(f: Float, i: Int)

    fun drawStarting(f: Float, i: Int)

    fun flushCanvas()

    fun forceSurfaceUnlockWorkaround()

    val currTouchPoint: ep?

    val directSurfaceRendering: Boolean

    val gameThreadSync: Any?

    var inGameActivity: InGameActivity?

    fun getNewCanvasLock(z: Boolean): l?

    val renderer: a?

    val surfaceExists: Boolean

    val isFullscreen: Boolean

    val isPaused: Boolean

    fun onParentPause()

    fun onParentResume()

    fun onParentStart()

    fun onParentStop()

    fun onParentWindowFocusChanged(z: Boolean)

    fun onReplacedByAnotherView()

    fun unlockAndReturnCanvas(lVar: l?, z: Boolean)

    fun updateResolution()

    fun usingBasicDraw(): Boolean

    override fun g(): Any? = gameThreadSync

    override fun a(p0: Boolean) {
        onParentWindowFocusChanged(p0)
    }

    override fun a(p0: l?, p1: Boolean) {
        unlockAndReturnCanvas(p0, p1)
    }

    override fun a() {
        onParentResume()
    }

    override fun j() {
        onReplacedByAnotherView()
    }

    override fun m() {
        updateResolution()
    }

    override fun b(): Boolean = surfaceExists

    override fun e(): Boolean = isPaused

    override fun c(): Boolean = directSurfaceRendering

    override fun d(): a? = renderer

    override fun f(): Boolean = isFullscreen

    override fun h() {
        forceSurfaceUnlockWorkaround()
    }

    override fun n(): Boolean = usingBasicDraw()

    override fun b(p0: Boolean): l? = getNewCanvasLock(p0)

    override fun a(p0: Float, p1: Int) {
        drawStarting(p0, p1)
    }

    override fun b(p0: Float, p1: Int) {
        drawCompleted(p0, p1)
    }
}