/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.corrodinggames.rts.appFramework.*;
import com.corrodinggames.rts.appFramework.android.AndroidSAF;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

/* loaded from: D:\steam\steamapps\common\Rusted Warfare\classes.dex */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, IView, eo {
    public ep currTouchPoint;
    int fullHeight;
    int fullWidth;
    public Object gameThreadSync;
    public InGameActivity inGameActivity;
    Method lockHardwareCanvasMethod;
    public en multiTouchController;
    public volatile boolean paused;
    public volatile boolean surfaceExists;
    SurfaceHolder surfaceHolderOnLock;

    public String getStats() {
        return "NO STATS";
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void onParentWindowFocusChanged(boolean z) {
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void onParentStart() {
        this.paused = false;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void onParentStop() {
        this.paused = true;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void onReplacedByAnotherView() {
        this.paused = true;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void onParentResume() {
        this.paused = false;
    }

    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.surfaceExists = false;
        this.gameThreadSync = new Object();
        this.fullWidth = -1;
        this.fullHeight = -1;
        this.paused = false;
        Log.e(AndroidSAF.TAG, "GameView:GameView()");
        this.multiTouchController = new en(this);
        this.currTouchPoint = new ep();
        init(context);
    }

    void init(Context context) {
        getHolder().addCallback(this);
        com.corrodinggames.rts.gameFramework.k.c(context);
    }

    protected void finalize() {
        Log.e(AndroidSAF.TAG, "GameView:finalize()");
        super.finalize();
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.fullWidth = i;
        this.fullHeight = i2;
        updateResolution();
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void updateResolution() {
        if (this.fullWidth != -1) {
            com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
            int i = this.fullWidth;
            int i2 = this.fullHeight;
            if (t.bN.renderDoubleScale) {
                i = this.fullWidth / 2;
                i2 = this.fullHeight / 2;
            }
            if (this.surfaceExists) {
                getHolder().setFixedSize(i, i2);
            } else {
                com.corrodinggames.rts.gameFramework.k.e("updateResolution surfaceExists==false");
            }
            t.a(i, i2);
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        com.corrodinggames.rts.gameFramework.k.e("GameView:surfaceCreated start - " + hashCode());
        com.corrodinggames.rts.gameFramework.k.t().bA = true;
        this.surfaceExists = true;
        updateResolution();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        com.corrodinggames.rts.gameFramework.k.e("GameView:surfaceDestroyed start - " + hashCode());
        synchronized (this.gameThreadSync) {
            t.bA = false;
            this.surfaceExists = false;
            com.corrodinggames.rts.gameFramework.k.e("GameEngine catch currentGameView.gameThreadSync - " + this.gameThreadSync.hashCode());
            getHolder().getSurface().release();
        }
        com.corrodinggames.rts.gameFramework.k.e("GameView:surfaceDestroyed finished - " + hashCode());
        if (this.surfaceHolderOnLock != null) {
            com.corrodinggames.rts.gameFramework.k.e("GameView:surfaceDestroyed - Error lock is still open");
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (Build.VERSION.SDK_INT >= 9) {
            com.corrodinggames.rts.gameFramework.k.d("onTouchEvent: Source:" + motionEvent.getSource());
            if (motionEvent.getSource() == 2) {
                com.corrodinggames.rts.gameFramework.k.d("onTouchEvent: InputDevice.SOURCE_CLASS_POINTER");
            }
            if (motionEvent.getSource() == 8194) {
                com.corrodinggames.rts.gameFramework.k.d("onTouchEvent: InputDevice.SOURCE_MOUSE");
            }
        }
        return this.multiTouchController.a(motionEvent);
    }

    @Override // com.corrodinggames.rts.appFramework.eo
    public Object getDraggableObjectAtPoint(ep p) {
        return this;
    }

    @Override
    public void getPositionAndScale(Object o, eq eq) {

    }


    @Override // com.corrodinggames.rts.appFramework.eo
    public void selectObject(Object obj, ep p) {
        this.currTouchPoint.a(p);
    }

    @Override
    public boolean setPositionAndScale(Object o, eq eq, ep ep) {
        this.currTouchPoint.a(ep);
        return true;

    }


    @Override // com.corrodinggames.rts.appFramework.ab
    public void forceSurfaceUnlockWorkaround() {
        try {
            @SuppressLint("DiscouragedPrivateApi") Field declaredField = SurfaceView.class.getDeclaredField("mSurfaceLock");
            declaredField.setAccessible(true);
            ((ReentrantLock) declaredField.get(this)).unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public boolean getSurfaceExists() {
        return this.surfaceExists;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public boolean getDirectSurfaceRendering() {
        return true;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public com.corrodinggames.rts.gameFramework.m.a getRenderer() {
        return null;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void onParentPause() {

    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public Object getGameThreadSync() {
        return this.gameThreadSync;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public InGameActivity getInGameActivity() {
        return this.inGameActivity;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void setInGameActivity(InGameActivity inGameActivity) {
        this.inGameActivity = inGameActivity;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public ep getCurrTouchPoint() {
        return this.currTouchPoint;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void drawStarting(float f, int i) {
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void drawCompleted(float f, int i) {
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void flushCanvas() {
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public com.corrodinggames.rts.gameFramework.m.l getNewCanvasLock(boolean z) {
        Canvas lockCanvas;
        if (!this.surfaceExists) {
            com.corrodinggames.rts.gameFramework.k.e("getNewCanvasLock: No surface ready");
            return null;
        }
        this.surfaceHolderOnLock = getHolder();
        if (Build.VERSION.SDK_INT >= 26) {
            if (this.lockHardwareCanvasMethod == null) {
                try {
                    this.lockHardwareCanvasMethod = SurfaceHolder.class.getMethod("lockHardwareCanvas", new Class[0]);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (SecurityException e2) {
                    throw new RuntimeException(e2);
                }
            }
            try {
                lockCanvas = (Canvas) this.lockHardwareCanvasMethod.invoke(this.surfaceHolderOnLock, new Object[0]);
            } catch (Exception e3) {
                throw new RuntimeException(e3);
            }
        } else {
            lockCanvas = this.surfaceHolderOnLock.lockCanvas();
        }
        if (lockCanvas == null) {
            com.corrodinggames.rts.gameFramework.k.a("getNewCanvasLock: Error surfaceHolder.lockCanvas==null");
            return null;
        }
        return new com.corrodinggames.rts.gameFramework.m.h(lockCanvas);
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public void unlockAndReturnCanvas(com.corrodinggames.rts.gameFramework.m.l lVar, boolean z) {
        try {
            this.surfaceHolderOnLock.unlockCanvasAndPost(((com.corrodinggames.rts.gameFramework.m.h) lVar).f794a);
            this.surfaceHolderOnLock = null;
        } catch (Exception e) {
            throw new RuntimeException("surfaceExists=" + this.surfaceExists + ", source=" + (((com.corrodinggames.rts.gameFramework.m.h) lVar).f794a != null) + ", hash=" + hashCode(), e);
        }
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public boolean usingBasicDraw() {
        return false;
    }

    @Override // com.corrodinggames.rts.appFramework.ab
    public boolean isFullscreen() {
        return false;
    }

    public void onNewWindow() {
    }
}