/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: D:\steam\steamapps\common\Rusted Warfare\classes.dex */
public class BaseGameActivity extends Activity {
    ArrayList resumeCallbacks = new ArrayList();

    public void addResumeCallback(Runnable runnable) {
        synchronized (this.resumeCallbacks) {
            this.resumeCallbacks.add(runnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        d.a((Activity) this);
        synchronized (this.resumeCallbacks) {
            if (this.resumeCallbacks.size() > 0) {
                Iterator it = this.resumeCallbacks.iterator();
                while (it.hasNext()) {
                    ((Runnable) it.next()).run();
                }
                this.resumeCallbacks.clear();
            }
        }
    }
}