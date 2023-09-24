/*
 * Copyright 2023 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.content.Intent
import com.corrodinggames.rts.appFramework.InGameActivity
import com.corrodinggames.rts.appFramework.LevelSelectActivity
import com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity
import com.corrodinggames.rts.game.a.a
import com.corrodinggames.rts.gameFramework.j.c
import com.corrodinggames.rts.gameFramework.k
import io.github.rwpp.android.MainActivity
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.RefreshUIEvent
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.FogMode
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.map.NetworkMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress


class GameRoomImpl(private val game: GameImpl) : GameRoom {
    private var playerCacheMap = mutableMapOf<PlayerInternal, Player>()
    override var maxPlayerCount: Int
        get() = PlayerInternal.c
        set(value) { PlayerInternal.b(value, true) }
    override val isHost: Boolean
        get() = GameEngine.t().bU.D
    override val isHostServer: Boolean
        get() = GameEngine.t().bU.I
    override val localPlayer: Player
        get() {
            val t = GameEngine.t()
            val p = playerCacheMap[t.bU.A]
            if(p == null) getPlayers()
            return playerCacheMap[t.bU.A]!!
        }
    override var sharedControl: Boolean
        get() = GameEngine.t().bU.aA.l
        set(value) { GameEngine.t().bU.aA.l = value }
    override val randomSeed: Int
        get() = GameEngine.t().bU.aA.q
    override val mapType: MapType
        get() = MapType.entries[GameEngine.t().bU.aA.a.ordinal]
    override var selectedMap: GameMap
        get() = game.getAllMaps().firstOrNull { (it.mapName + ".tmx").replace("\\", "/").endsWith(GameEngine.t().bU.aA.b ?: "") }
            ?: NetworkMap(LevelSelectActivity.convertLevelFileNameForDisplay(GameEngine.t().bU.aA.b))
        set(value) {
            val realPath = (value.mapName + ".tmx").replace("\\", "/")
            GameEngine.t().bU.aB = realPath
            GameEngine.t().bU.aA.a = com.corrodinggames.rts.gameFramework.j.at.entries[value.mapType.ordinal]
            GameEngine.t().bU.aA.b = (value.mapName + ".tmx")
        }
    override var startingCredits: Int
        get() = GameEngine.t().bU.aA.c
        set(value) { GameEngine.t().bU.aA.c = value }
    override var startingUnits: Int
        get() = GameEngine.t().bU.aA.g
        set(value) { GameEngine.t().bU.aA.g = value }
    override var fogMode: FogMode
        get() = FogMode.entries[GameEngine.t().bU.aA.d.coerceAtLeast(0)]
        set(value) { GameEngine.t().bU.aA.d = value.ordinal }
    override val revealedMap: Boolean
        get() = GameEngine.t().bU.aA.e
    override var aiDifficulty: Difficulty
        get() = Difficulty.entries[GameEngine.t().bU.aA.f]
        set(value) {  GameEngine.t().bU.aA.f = value.ordinal - 2}
    override var incomeMultiplier: Float
        get() = GameEngine.t().bU.aA.h
        set(value) { GameEngine.t().bU.aA.h = value}
    override var noNukes: Boolean
        get() = GameEngine.t().bU.aA.i
        set(value) {  GameEngine.t().bU.aA.i = value }
    override var allowSpectators: Boolean
        get() = GameEngine.t().bU.aA.o
        set(value) { GameEngine.t().bU.aA.o = value }
    override var lockedRoom: Boolean
        get() = GameEngine.t().bU.aA.p
        set(value) {  GameEngine.t().bU.aA.p = value }
    override var teamLock: Boolean
        get() = GameEngine.t().bU.aA.m
        set(value) { GameEngine.t().bU.aA.m = value }

    override fun getPlayers(): List<Player> {
        return PlayerInternal.j.mapNotNull {
            if(it == null) return@mapNotNull null
            playerCacheMap.getOrPut(it) { PlayerImpl(it, this) }
        }
    }

    override suspend fun roomDetails(): String = withContext(Dispatchers.IO) {
        val t = GameEngine.t()
        val bU = GameEngine.t().bU
        """
            ${if(isHost) "Local IP address: ${InetAddress.getLocalHost().hostAddress} port: ${bU.m}" else ""}
            ${if(isHost) "Your public address is <${if(bU.aW != null && bU.aW == true) "OPEN" else "CLOSED"}> to the internet" else ""}
            Starting Credits: ${com.corrodinggames.rts.gameFramework.j.ae.c(bU.aA.g)}
            Fog: ${fogMode.name}
            ${incomeMultiplier}X income
            ${if(noNukes) "No nukes" else ""}
            Shared control: $sharedControl
        """.trimIndent()
    }

    override fun sendChatMessage(message: String) {
        GameEngine.t().bU.k(message)
    }

    override fun sendSystemMessage(message: String) {
        GameEngine.t().bU.h(message)
    }

    override fun addAI() {
        val t: k = GameEngine.t()
        if(!t.bU.D) {
            if(t.bU.I) {
                t.bU.i("-addai")
                return
            } else {
//                com.corrodinggames.rts.gameFramework.k.a(
//                    "addAI.setOnClickListener",
//                    "Clicked but not server or proxy controller"
//                )
                return
            }
        }
        val aeVar = t.bU
        if(!aeVar.D) {
            //com.corrodinggames.rts.gameFramework.k.a("addAIToGame", "We are not a server")
            return
        }
        val y: Int = PlayerInternal.y()
        if(y != -1) {
            //t2.g("No free slots for AI")
            val aVar: a = a(y)
            aVar.w = "AI"
            aVar.s = y % 2
            aVar.y = aeVar.aA.f
            aeVar.B()
            t.bU.b(null as c?)
            RefreshUIEvent().broadCastIn()
        }
    }

    override fun applyTeamChange(mode: String) {
        val layout = when(mode) {
            "2t" -> com.corrodinggames.rts.gameFramework.j.ba.a
            "3t" -> com.corrodinggames.rts.gameFramework.j.ba.b
            "FFA" -> com.corrodinggames.rts.gameFramework.j.ba.c
            "spectators" -> com.corrodinggames.rts.gameFramework.j.ba.d
            else -> throw RuntimeException()
        }

        GameEngine.t().bU.a(layout)
    }

    override fun kickPlayer(player: Player) {
        GameEngine.t().bU.d((player as PlayerImpl).player)
    }

    override fun disconnect() {
        GameEngine.t().bU.b("exited")
    }

    override fun startGame() {
        val t: k = GameEngine.t()
        MultiplayerBattleroomActivity.startGameCommon()
        if(t.bI != null && t.bI.X) {
            t.bU.bf = true
            //GameEngine.K()
            val intent = Intent(MainActivity.instance, InGameActivity::class.java)
            intent.putExtra("level", t.di)
            MainActivity.instance.startActivityForResult(intent, 0)

            //MainActivity.gameLauncher.launch(intent)
            return
        }
        //d("Not starting multiplayer game because map failed to load")
        val aeVar = t.bU
        aeVar.be = true
        //d("onStartGameFailed")
        if(!aeVar.D) {
            aeVar.b("Map load failed")
            return
        }
        aeVar.aY = false
        aeVar.h("Map load failed.")


    }
}