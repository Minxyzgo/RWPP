/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */
package io.github.rwpp.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import com.corrodinggames.rts.appFramework.android.AndroidSAF
import com.corrodinggames.rts.gameFramework.h.a
import com.corrodinggames.rts.gameFramework.k.c
import com.corrodinggames.rts.gameFramework.k.d
import io.github.rwpp.impl.GameEngine
import io.github.rwpp.logger


/* loaded from: D:\steam\steamapps\common\Rusted Warfare\classes.dex */
class InGameActivity : BaseGameActivity() {
    var gameViewCommon: IView? = null
    var progressDialog: ProgressDialog? = null
    val uiHandler: Handler = Handler(Looper.getMainLooper())
    var test: Boolean = true

    // android.app.Activity
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        if (setupActivity(this)) {
            setContentView(io.github.rwpp.R.layout.main)
            window.setBackgroundDrawable(null)
            this.gameViewCommon = buildGameView(this)
            gameViewCommon!!.inGameActivity = this
        }
    }

    // android.app.Activity
    override fun finish() {
        logger.info("IngameActivity: finish")
        super.finish()
        this.overridePendingTransition(0, 0)
    }

    // android.app.Activity
    override fun onStart() {
        super.onStart()
        Log.e(AndroidSAF.TAG, "Ingame:onStart")
        GameEngine.B()?.c(gameViewCommon as Context?)
    }

    // android.app.Activity
    override fun onStop() {
        super.onStop()
        Log.e(AndroidSAF.TAG, "Ingame:onStop")
//        val t: k = com.corrodinggames.rts.gameFramework.k.t()
//        if (t != null) {
//            t.a(this, this.gameViewCommon)
//        }
    }

    // android.app.Activity
    override fun onPause() {
        Log.e(AndroidSAF.TAG, "Ingame:onPause")
//        val t: k = com.corrodinggames.rts.gameFramework.k.t()
//        if (t != null) {
//            t.b(this.gameViewCommon)
//        }
//        gameViewCommon!!.onParentPause()
        super.onPause()
    }

    // android.app.Activity, android.view.Window.Callback
    override fun onWindowFocusChanged(z: Boolean) {
        super.onWindowFocusChanged(z)
//        if (z) {
//            d.a(this as Activity, false, true)
//        }
//        gameViewCommon!!.onParentWindowFocusChanged(z)
    }

    // com.corrodinggames.rts.appFramework.a, android.app.Activity
    override fun onResume() {
        Log.e(AndroidSAF.TAG, "Ingame:onResume")
        super.onResume()
//        val c: k = c(this)
//        if (c != null) {
//            this.gameViewCommon = d.a(this, this.gameViewCommon)
//            gameViewCommon!!.inGameActivity = this
//            c.a(this as Activity, this.gameViewCommon, false)
//        }
//        d.a(this as Activity, false, true)
    }

    // android.app.Activity
    public override fun onDestroy() {
        Log.e(AndroidSAF.TAG, "InGameActivity:onDestroy")
       // com.corrodinggames.rts.gameFramework.k.t()
        super.onDestroy()
    }

    // android.app.Activity
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        super.onPrepareOptionsMenu(menu)
//        menu.clear()
//        val t: k = com.corrodinggames.rts.gameFramework.k.t()
//        menu.add(0, 12, 0, a.a("menus.ingame.save", arrayOfNulls<Any>(0))).setIcon(android.R.drawable.ic_menu_save)
//        if (t.bs && !com.corrodinggames.rts.gameFramework.k.aW) {
//            menu.add(0, 18, 0, a.a("menus.ingame.exportMap", arrayOfNulls<Any>(0)))
//                .setIcon(android.R.drawable.ic_menu_save)
//        }
//        menu.add(0, 2, 0, a.a("menus.ingame.settings", arrayOfNulls<Any>(0)))
//            .setIcon(android.R.drawable.ic_menu_preferences)
//        t.E()
//        if (t.bY != null && t.bY.g()) {
//            menu.add(0, 22, 0, a.a("menus.ingame.hideInterface", arrayOfNulls<Any>(0)))
//                .setIcon(android.R.drawable.ic_menu_send)
//        }
//        if (t.E()) {
//            menu.add(0, 13, 0, a.a("menus.ingame.chat", arrayOfNulls<Any>(0))).setIcon(android.R.drawable.ic_menu_send)
//            menu.add(0, 14, 0, a.a("menus.ingame.players", arrayOfNulls<Any>(0)))
//                .setIcon(android.R.drawable.ic_menu_sort_by_size)
//            if (t.bU.D) {
//                com.corrodinggames.rts.gameFramework.o.a.a()
//            }
//            if (!(t.bp != null && t.bp.J) && !t.dn) {
//                menu.add(0, 19, 0, a.a("menus.ingame.surrender", arrayOfNulls<Any>(0)))
//                    .setIcon(android.R.drawable.ic_lock_power_off)
//            }
//            if (!t.bU.D) {
//                menu.add(0, 10, 0, a.a("menus.ingame.disconnect", arrayOfNulls<Any>(0)))
//                    .setIcon(android.R.drawable.ic_lock_power_off)
//            } else {
//                menu.add(0, 10, 0, a.a("menus.ingame.exitGame", arrayOfNulls<Any>(0)))
//                    .setIcon(android.R.drawable.ic_lock_power_off)
//            }
//        } else {
//            if (t.cb != null && t.cb.h != null) {
//                menu.add(0, 11, 0, a.a("menus.ingame.briefing", arrayOfNulls<Any>(0)))
//                    .setIcon(android.R.drawable.ic_dialog_info)
//            }
//            menu.add(0, 15, 0, a.a("menus.ingame.exitGame", arrayOfNulls<Any>(0)))
//                .setIcon(android.R.drawable.ic_lock_power_off)
//        }
//        if (t != null && t.bN.allowGameRecording) {
//            if (!t.bl) {
//                menu.add(0, 9, 0, "Start Recording")
//            } else {
//                menu.add(0, 9, 0, "Stop Recording")
//            }
//        }
        return true
    }

    // android.app.Activity
    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        selectMenuOptionInternal(menuItem.itemId)
        return super.onOptionsItemSelected(menuItem)
    }

    fun selectMenuOption(i: Int) {
//        d("outer selectMenuOption: $i")
//        uiHandler.post(ak(this, i))
    }

    fun selectMenuOptionInternal(i: Int) {
//        when (i) {
//            2 -> {
//                startActivityForResult(
//                    Intent(
//                        baseContext,
//                        SettingsActivity::class.java as Class<*>
//                    ), 0
//                )
//                return
//            }
//
//            3 -> {
//                AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Skip?")
//                    .setMessage("Are you sure you want to skip this level?").setPositiveButton(
//                        "Yes", ax(
//                            this
//                        )
//                    ).setNegativeButton("No", null as DialogInterface.OnClickListener?).show()
//                return
//            }
//
//            4 -> {
//                com.corrodinggames.rts.gameFramework.k.t().ce =
//                    if (com.corrodinggames.rts.gameFramework.k.t().ce) false else true
//                return
//            }
//
//            5 -> {
//                AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Restart?")
//                    .setMessage("Are you sure you want to restart this level?").setPositiveButton(
//                        "Yes", ay(
//                            this
//                        )
//                    ).setNegativeButton("No", null as DialogInterface.OnClickListener?).show()
//                return
//            }
//
//            6 -> {
//                val t: k = com.corrodinggames.rts.gameFramework.k.t()
//                t.bi = if (t.bi) false else true
//                return
//            }
//
//            7, 8, 17 -> return
//            9 -> {
//                val t2: k = com.corrodinggames.rts.gameFramework.k.t()
//                if (!t2.bl) {
//                    t2.bl = true
//                    return
//                } else {
//                    t2.bl = false
//                    return
//                }
//            }
//
//            10 -> {
//                val t3: k = com.corrodinggames.rts.gameFramework.k.t()
//                var a2 = a.a("menus.ingame.multiplayerClose.titleDisconnect", arrayOfNulls<Any>(0))
//                var a3 = a.a("menus.ingame.multiplayerClose.messageDisconnect", arrayOfNulls<Any>(0))
//                var a4 = a.a("menus.ingame.multiplayerClose.disconnectButton", arrayOfNulls<Any>(0))
//                if (t3.bU.D) {
//                    a2 = a.a("menus.ingame.multiplayerClose.title", arrayOfNulls<Any>(0))
//                    a3 = a.a("menus.ingame.multiplayerClose.messageEndGame", arrayOfNulls<Any>(0))
//                    a4 = a.a("menus.ingame.exitGame", arrayOfNulls<Any>(0))
//                }
//                val negativeButton =
//                    AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(a2).setMessage(a3)
//                        .setPositiveButton(
//                            a4, bb(
//                                this
//                            )
//                        ).setNegativeButton(
//                            a.a("menus.common.back", arrayOfNulls<Any>(0)),
//                            null as DialogInterface.OnClickListener?
//                        )
//                if (t3.bU.D) {
//                    negativeButton.setNeutralButton(
//                        a.a("menus.ingame.multiplayerClose.returnToBattleroom", arrayOfNulls<Any>(0)), bc(
//                            this
//                        )
//                    )
//                }
//                negativeButton.show()
//                return
//            }
//
//            11 -> {
//                val t4: k = com.corrodinggames.rts.gameFramework.k.t()
//                if (t4.cb != null && t4.cb.h != null) {
//                    t4.a("Briefing", t4.cb.h)
//                    return
//                }
//                return
//            }
//
//            12 -> {
//                val azVar: az = az(this, this)
//                if (!d.a(this, azVar)) {
//                    azVar.run()
//                    return
//                }
//                return
//            }
//
//            13 -> {
//                makeSendMessagePopup(false)
//                return
//            }
//
//            14 -> {
//                if (com.corrodinggames.rts.gameFramework.k.t().bU != null) {
//                    ae.j()
//                    return
//                }
//                return
//            }
//
//            15 -> {
//                AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit?")
//                    .setMessage("Are you sure you want to exit this game?").setPositiveButton(
//                        "Yes", bd(
//                            this
//                        )
//                    ).setNegativeButton("No", null as DialogInterface.OnClickListener?).show()
//                return
//            }
//
//            16 -> {
//                makeSendMessagePopup(true)
//                return
//            }
//
//            18 -> {
//                if (d.e(this)) {
//                    makeExportMapPopup(null)
//                    return
//                }
//                return
//            }
//
//            19 -> {
//                AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Disconnect?")
//                    .setMessage("Are you sure you want to surrender this game?").setPositiveButton(
//                        "Surrender", ba(
//                            this
//                        )
//                    ).setNegativeButton("No", null as DialogInterface.OnClickListener?).show()
//                return
//            }
//
//            20 -> {
//                finish()
//                return
//            }
//
//            21 -> {
//                finish()
//                MultiplayerBattleroomActivity.updateUI()
//                MultiplayerBattleroomActivity.refreshChatLog()
//                return
//            }
//
//            22 -> {
//                val t5: k = com.corrodinggames.rts.gameFramework.k.t()
//                t5.cR = true
//                t5.bP.u = false
//                return
//            }
//
//            23 -> {
//                d("TODO display leaderboard settings")
//                return
//            }
//
//            else -> return
//        }
    }

    // android.app.Activity, android.view.KeyEvent.Callback
    override fun onKeyDown(i: Int, keyEvent: KeyEvent): Boolean {
//        if (i == 82) {
//            return super.onKeyDown(i, keyEvent)
//        }
//        if (i == 25) {
//            return super.onKeyDown(i, keyEvent)
//        }
//        if (i == 24) {
//            return super.onKeyDown(i, keyEvent)
//        }
//        if (i == 84) {
//            val t: k = com.corrodinggames.rts.gameFramework.k.t()
//            if (t.cS === 1.0f) {
//                t.cS = 1.5f
//            } else if (t.cS === 1.5f) {
//                t.cS = 0.75f
//            } else {
//                t.cS = 1.0f
//            }
//        }
//        if (i == 4) {
//            if (Build.VERSION.SDK_INT >= 12 && keyEvent.source == 8194) {
//                d("KEYCODE_BACK from mouse")
//                val t2: k = com.corrodinggames.rts.gameFramework.k.t()
//                gameViewCommon!!.currTouchPoint.a(t2.dO, t2.dP, true)
//                return true
//            }
//            onBackPressed()
//        }
//        return com.corrodinggames.rts.gameFramework.k.t().a(i, keyEvent)
        return true
    }

    // android.app.Activity, android.view.KeyEvent.Callback
    override fun onKeyUp(i: Int, keyEvent: KeyEvent): Boolean {
//        if (i == 4 && Build.VERSION.SDK_INT >= 12 && keyEvent.source == 8194) {
//            d("onKeyUp from mouse: KEYCODE_BACK")
//            val t: k = com.corrodinggames.rts.gameFramework.k.t()
//            gameViewCommon!!.currTouchPoint.a(t.dO, t.dP, false)
//        }
//        return if (i == 82) super.onKeyDown(i, keyEvent) else com.corrodinggames.rts.gameFramework.k.t().b(i, keyEvent)
        return true
    }

    // android.app.Activity
    override fun onTrackballEvent(motionEvent: MotionEvent): Boolean {
//        val t: k = com.corrodinggames.rts.gameFramework.k.t()
//        t.de += motionEvent.x
//        t.df += motionEvent.y
//        return if (motionEvent.action == 0 || motionEvent.action == 1) false else true
        return true
    }

    @SuppressLint("InflateParams")
    private fun makeSendMessagePopup(z: Boolean) {
//        val t: k = com.corrodinggames.rts.gameFramework.k.t()
//        val builder = AlertDialog.Builder(this)
//        if (!z) {
//            builder.setTitle("Send Message")
//        } else {
//            builder.setTitle("Send Team Message")
//        }
//        val inflate: View = LayoutInflater.from(this).inflate(R.layout.alert_chat, null as ViewGroup?)
//        builder.setView(inflate)
//        val textView = inflate.findViewById<View>(R.id.chat_messages) as TextView
//        val editText = inflate.findViewById<View>(R.id.chat_text) as EditText
//        textView.setText(t.bU.aE.a())
//        editText.setText(VariableScope.nullOrMissingString)
//        editText.requestFocus()
//        builder.setPositiveButton(if (z) "Send Team" else "Send", be(this, editText, z))
//        builder.setNeutralButton("Send & Ping Map", al(this, editText, z))
//        builder.setNegativeButton("Cancel", am(this))
//        builder.show()
    }

    /* JADX INFO: Access modifiers changed from: private */
    fun makeExportMapPopup(str: String?) {
//        var str = str
//        val t: k = com.corrodinggames.rts.gameFramework.k.t()
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Export Map")
//        builder.setMessage("Enter a name to export the map as")
//        val editText = EditText(this)
//        if (str == null) {
//            str = ("New " + t.V() + " (" + com.corrodinggames.rts.gameFramework.f.a("d MMM yyyy").replace(
//                ".",
//                VariableScope.nullOrMissingString
//            ) + " " + com.corrodinggames.rts.gameFramework.f.a("HH.mm.ss") + ")").replace("  ", " ")
//        }
//        editText.setText(str)
//        builder.setView(editText)
//        builder.setPositiveButton("Ok", an(this, editText, t))
//        builder.setNegativeButton("Cancel", ap(this))
//        builder.show()
    }

    /* JADX INFO: Access modifiers changed from: private */
    fun makeSaveGamePopup(str: String?) {
//        val t: k = com.corrodinggames.rts.gameFramework.k.t()
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Save Game")
//        builder.setMessage("Enter a name to save the game under")
//        val editText = EditText(this)
//        if (str == null) {
//            editText.setText(
//                t.V() + " (" + com.corrodinggames.rts.gameFramework.f.a("d MMM yyyy").replace(
//                    ".",
//                    VariableScope.nullOrMissingString
//                ) + " " + com.corrodinggames.rts.gameFramework.f.a("HH.mm.ss") + ")"
//            )
//        } else {
//            editText.setText(str)
//        }
//        builder.setView(editText)
//        builder.setPositiveButton("Ok", aq(this, editText))
//        builder.setNegativeButton("Cancel", `as`(this))
//        builder.show()
    }

    // android.app.Activity
    override fun onCreateDialog(i: Int): Dialog? {
        when (i) {
            0 -> {
                this.progressDialog = ProgressDialog(this)
                progressDialog!!.setProgressStyle(0)
                progressDialog!!.setMessage("Saving...")
                progressDialog!!.setCancelable(false)
                return this.progressDialog
            }

            else -> return null
        }

    }

    fun saveGame(str: String) {
//        showDialog(0)
//        val bfVar: bf = bf(this)
//        bfVar.f61a = str
//        Thread(bfVar).start()
    }

    // android.app.Activity
    override fun onBackPressed() {
//        if (com.corrodinggames.rts.gameFramework.k.t().bU.C) {
//            val builder = AlertDialog.Builder(this)
//            builder.setIcon(android.R.drawable.ic_dialog_info)
//            builder.setTitle(a.a("menus.ingame.multiplayerClose.title", arrayOfNulls<Any>(0)))
//            builder.setMessage(a.a("menus.ingame.multiplayerClose.message", arrayOfNulls<Any>(0)))
//            builder.setPositiveButton(
//                a.a("menus.ingame.multiplayerClose.disconnectButton", arrayOfNulls<Any>(0)), at(
//                    this
//                )
//            )
//            builder.setNeutralButton(
//                a.a("menus.ingame.multiplayerClose.minimizeButton", arrayOfNulls<Any>(0)), au(
//                    this
//                )
//            )
//            builder.setNegativeButton(
//                a.a("menus.ingame.multiplayerClose.stayButton", arrayOfNulls<Any>(0)), av(
//                    this
//                )
//            )
//            builder.show()
//            return
//        }
//        finish()
    }

    fun openMarketLink() {
//        uiHandler.post(aw(this))
    }

    /* JADX INFO: Access modifiers changed from: private */
    fun openMarketLinkInternal() {
//        try {
//            startActivity(Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.corrodinggames.rts")))
//        } catch (e: ActivityNotFoundException) {
//            Toast.makeText(applicationContext, "Failed to open Android Market", 0).show()
//        }
    }

    fun showPCMainMenu() {
    }

    fun showLeaderboardSettingsWindow() {
    }

    companion object {
        const val DISCONNECT_ID: Int = 10
        const val EXIT_GAME_ID: Int = 15
        const val FULL_SAVE_ID: Int = 12
        const val HIDE_INTERFACE_ID: Int = 22
        const val LIST_PLAYERS_ID: Int = 14
        const val LOOK_ID: Int = 4
        const val MODE_ID: Int = 6
        const val PICKTILE_ID: Int = 1
        const val QUICK_LOAD_ID: Int = 8
        const val QUICK_SAVE_ID: Int = 7
        const val RECORD_ID: Int = 9
        const val RESTART_ID: Int = 5
        const val SAVE_MAP_ID: Int = 18
        const val SAVING_DIALOG: Int = 0
        const val SEND_MESSAGE_ID: Int = 13
        const val SEND_TEAM_MESSAGE_ID: Int = 16
        const val SETTINGS_ID: Int = 2
        const val SHOW_BATTLE_ROOM: Int = 21
        const val SHOW_BRIEFING_ID: Int = 11
        const val SHOW_LEADERBOARD_ID: Int = 23
        const val SHOW_MAIN_MENU: Int = 20
        const val SKIP_ID: Int = 3
        const val STEAM_REINVITE_ID: Int = 17
        const val SURRENDER_ID: Int = 19
    }
}