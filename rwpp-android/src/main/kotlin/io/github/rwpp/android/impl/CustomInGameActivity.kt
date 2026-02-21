/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.corrodinggames.rts.appFramework.InGameActivity
import io.github.rwpp.android.OffscreenSurfaceView
import io.github.rwpp.appKoin
import io.github.rwpp.config.Settings
import io.github.rwpp.ui.UI

class CustomInGameActivity : InGameActivity(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    private val localViewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore = localViewModelStore


    override fun onCreate(savedInstanceState: Bundle?) {
        savedStateRegistryController.performRestore(savedInstanceState)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        super.onCreate(savedInstanceState)
        instance = this

        val glSurfaceView = OffscreenSurfaceView(this)

        val fullScreenParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.FILL
        }

        addContentView(glSurfaceView, fullScreenParams)

        val composeView = ComposeView(this)
            .apply {
                setViewTreeLifecycleOwner(this@CustomInGameActivity)
                setViewTreeSavedStateRegistryOwner(this@CustomInGameActivity)
                setViewTreeViewModelStoreOwner(this@CustomInGameActivity)

                setContent {
                    UI.UiProvider.InGameComposeContent()
                }
            }

        addContentView(composeView, fullScreenParams)
    }

    private var isShiftPressed = false

    private val settings by lazy { appKoin.get<Settings>() }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (settings.enableVolumeKeyMapping) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (!isShiftPressed) {
                        isShiftPressed = true
                        dispatchSimulatedKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT)
                    }
                    return true
                }

                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    dispatchSimulatedKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_S)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (settings.enableVolumeKeyMapping) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    isShiftPressed = false
                    dispatchSimulatedKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT)
                    return true
                }

                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    dispatchSimulatedKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_S)
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun dispatchSimulatedKeyEvent(action: Int, keyCode: Int) {
        val time = SystemClock.uptimeMillis()
        val event = KeyEvent(
            time,  // downTime
            time,  // eventTime
            action,
            keyCode,
            0      // repeat
        )
        window.decorView.dispatchKeyEvent(event)
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onPause() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        savedStateRegistryController.performSave(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        super.onStop()
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
        super.onDestroy()
    }

    companion object {
        var instance: CustomInGameActivity? = null
    }
}