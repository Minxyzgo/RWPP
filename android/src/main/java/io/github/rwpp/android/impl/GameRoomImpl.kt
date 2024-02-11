/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.android.impl

import android.annotation.SuppressLint
import android.content.Intent
import com.corrodinggames.rts.appFramework.InGameActivity
import com.corrodinggames.rts.appFramework.LevelSelectActivity
import com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity
import com.corrodinggames.rts.game.a.a
import com.corrodinggames.rts.gameFramework.j.ae
import com.corrodinggames.rts.gameFramework.j.at
import com.corrodinggames.rts.gameFramework.j.bg
import com.corrodinggames.rts.gameFramework.j.c
import com.corrodinggames.rts.gameFramework.k
import io.github.rwpp.android.*
import io.github.rwpp.config.MultiplayerPreferences
import io.github.rwpp.config.instance
import io.github.rwpp.event.broadCastIn
import io.github.rwpp.event.events.RefreshUIEvent
import io.github.rwpp.game.ConnectingPlayer
import io.github.rwpp.game.GameRoom
import io.github.rwpp.game.Player
import io.github.rwpp.game.data.RoomOption
import io.github.rwpp.game.base.Difficulty
import io.github.rwpp.game.map.FogMode
import io.github.rwpp.game.map.GameMap
import io.github.rwpp.game.map.MapType
import io.github.rwpp.game.map.NetworkMap
import io.github.rwpp.welcomeMessage
import java.util.concurrent.ConcurrentLinkedQueue


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
            return playerCacheMap[t.bU.A] ?: ConnectingPlayer
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
            val realPath =
                (if(value.mapType == MapType.SkirmishMap) "maps/skirmish/" else "") +
                        (value.mapName + value.getMapSuffix()).replace("\\", "/")
            GameEngine.t().bU.aB = com.corrodinggames.rts.gameFramework.e.a.b.f(realPath)
            GameEngine.t().bU.aA.a = com.corrodinggames.rts.gameFramework.j.at.entries[value.mapType.ordinal]
            GameEngine.t().bU.aA.b = (value.mapName + value.getMapSuffix())
            GameEngine.t().bU.n()
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
        get() = Difficulty.entries[GameEngine.t().bU.aA.f + 2]
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
        set(value) {
            GameEngine.t().bU.aA.p = value
            if(isHost && value) sendSystemMessage("Room has been locked. Now player can't join the room")
        }
    override var teamLock: Boolean
        get() = GameEngine.t().bU.aA.m
        set(value) { GameEngine.t().bU.aA.m = value }
    override val mods: Array<String>
        get() = roomMods
    override var isRWPPRoom: Boolean = false
    override var option: RoomOption = RoomOption()
    override val isConnecting: Boolean
        get() = GameEngine.t().bU.C

    override fun getPlayers(): List<Player> {
        return PlayerInternal.j.mapNotNull {
            if(it == null) return@mapNotNull null
            playerCacheMap.getOrPut(it) {
                PlayerImpl(it, this)
                    .also { sendWelcomeMessage(it) }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override suspend fun roomDetails(): String {
        val n = 0;
        val t = k.t();
        val bu = t.bU;
        val t2 = k.t();
            run Label_1604@{
//                if(!t2.bU.D || t2.bU.G) {
//                    return@Label_1604;
//                }
                var y = ae.y();
                var s: String? = null
                if(y == null || y.size == 0) {
                    s = null;
                } else {
                    s = "";
                    val iterator = y.iterator();
                    var n2 = 1;
                    while(iterator.hasNext()) {
                        var s2 = iterator.next();
                        if(n2 != 0) {
                            n2 = 0;
                        } else {
                            s += ", ";
                        }
                        s += s2;
                    }
                }
                var s3: String = ""
                if(bu.E) {
                    if(bu.F == null) {
                        return@Label_1604;
                    }
                    s3 = "" + bu.F;
                } else if(isHost && !isSandboxGame) {
                    if(s != null) {
                        var string = "Local IP address: " + s + " port: " + t2.bU.m + "\n";
                        var s4: String? = null;
                        if(t2.bU.aX != null) {
                            if(!t2.bU.aX) {
                                s4 =
                                    string + "Unable to get a public IP address, check your internet connection" + "\n";
                            } else {
                                s4 = string;
                                if(t2.bU.aV != null) {
                                    s4 = string;
                                    if(t2.bU.aW != null) {
                                        val append = StringBuilder().append(string).append("Your public address is ");
                                        var s5: String? = null;
                                        if(t2.bU.aW) {
                                            s5 = "<Open>";
                                        } else {
                                            s5 = "<CLOSED>";
                                        }
                                        s4 = append.append(s5).append(" to the internet" + "\n").toString();
                                    }
                                }
                            }
                        } else {
                            s4 = string + if(isHost) "Retrieving your public IP..." + "\n" else "";
                        }
                        s3 = "" + s4;
                    } else {
                        s3 = "" + "You do not have a network connection" + "\n";
                    }
                }

                var s6 = s3;
                if(t2.G()) {
                    if(bu.p) {
                        s6 = s3 + "SandBox Mode! \n Place any unit, Control all teams, Special powers" + "\n";
                    } else {
                        s6 = s3 + "Local skirmish" + "\n";
                    }
                }
                var n3 = 0;
                if(k.X() && t2.bU.D) {
                    n3 = 0;
                } else {
                    n3 = 1;
                }
                var s7 = s6;
                if(s6.length != 0) {
                    s7 = s6 + ""
                    val s8 = s7

                    if(k.Z()) {
                        s7 = s8 + "";
                    }
                }
                var string2: String? = null;
                run Label_0977@{
                    if(!t2.bU.ax) {
                        string2 = s7;
                        if(!t2.bU.D) {
                            return@Label_0977;
                        }
                    }
                    var string3: String = s7;
                    if(n3 != 0) {
                        var string4 = s7;
                        if(t2.bU.aA.a != null) {
                            string4 = s7 + "Game Mode: " + t2.bU.aA.a.a() + "\n";
                        }
                        string3 = string4;
                        if(t2.bU.aA.b != null) {
                            string3 =
                                string4 + "Map: " + LevelSelectActivity.convertLevelFileNameForDisplay(t2.bU.aA.b) + "\n";
                        }
                    }
                    val append2 = StringBuilder().append(string3).append("\n" + "Starting Credits: ");

                    val bu2 = t2.bU;
                    var s9: String? = null;
                    if(bu2.aA.c == 0) {
                        s9 = "Default ($" + bu2.e() + ")";
                    } else {
                        s9 = "$" + bu2.e();
                    }
                    val append3 = StringBuilder().append(append2.append(s9).toString()).append("\n" + "Fog: ");
                    val bu3 = t2.bU;
                    var s10: String? = null
                    if(bu3.aA.d == 0) {
                        s10 = "No fog";
                    } else if(bu3.aA.d == 1) {
                        s10 = "Basic fog";
                    } else if(bu3.aA.d == 2) {
                        s10 = "Line of Sight";
                    } else {
                        s10 = "Unknown";
                    }
                    var s12: String? = null;
                    s12 = append3.append(s10).toString();
                    val s11 = s12
                    if(t2.bU.aA.g != 1) {
                        s12 = s11 + "Starting Units: " + ae.c(t2.bU.aA.g);
                    }
                    var string5 = s12;
                    if(t2.bU.aA.h != 1.0f) {
                        string5 = s12 + "\n" + incomeMultiplier + "X income";
                    }
                    var string6 = string5;
                    if(t2.bU.aA.i) {
                        string6 = string5 + "\n" + "No nukes" ;
                    }
                    var string7 = string6;
                    if(t2.bU.aA.l) {
                        string7 = string6 + "\n" + "Shared control: On";
                    }
                    string2 = string7;
                    if(bu.D) {
                        var string8 = string7;
                        if(t2.bU.n != null) {
                            string8 = string7 + "Password Protection: On" + "\n";
                        }
                        var string9 = string8;
                        if(!t2.bU.q) {
                            string9 = string8;
                            if(!t2.bU.G) {
                                string9 = string8 + "Server Visibility: Hidden" + "\n";
                            }
                        }


                        if(t2.bU.o) {
                            string2 = string9;
                            if(!t2.bU.G) {
                                val i = t2.bW.i();
                                var s13 = string9 + "-- Required Mods: --" + "\n";
                                val iterator2 = i.iterator();
                                var n4 = n;
                                while(iterator2.hasNext()) {
                                    val b = iterator2.next();
                                    if(n4 > 2 && n4 < i.size - 1) {
                                        string2 = s13 + (i.size - n4) + " more mods...";
                                        return@Label_0977;
                                    }
                                    ++n4;
                                    var b2 = (b as com.corrodinggames.rts.gameFramework.i.b).b();
                                    b2.replace("\"", "'");
                                    b2.replace(";", ".");
                                    s13 = s13 + " mod: $b2";
                                }
                                string2 = s13;
                            }
                        }
                    }
                }
                return string2!!
            }

        return "Getting details..."
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
        isSandboxGame = false
        if(isConnecting) GameEngine.t().bU.b("exited")
        isRWPPRoom = false
        option = RoomOption()
        roomMods = arrayOf()
        // 刷新地图
        GameEngine.t().bU.aA.a = at.a
        GameEngine.t().bU.aB = "maps/skirmish/[z;p10]Crossing Large (10p).tmx"
        GameEngine.t().bU.aA.b = "[z;p10]Crossing Large (10p).tmx"
        MainActivity.activityResume()
    }

    override fun startGame() {
        val t: k = GameEngine.t()
        isGaming = true

        if(isHost || isSandboxGame) {
            t.bU.q()
            t.bU.n()
            t.bU.a(null, false)
        }

        MultiplayerBattleroomActivity.startGameCommon()
        if(t.bI != null && t.bI.X) {
            t.bU.bf = true
            //GameEngine.K()
            val intent = Intent(MainActivity.instance, InGameActivity::class.java)
            intent.putExtra("level", t.di)
            gameLauncher.launch(intent)
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

    private fun sendWelcomeMessage(p: PlayerImpl) {
        if(MultiplayerPreferences.instance.showWelcomeMessage != true) return
        val conn = (GameEngine.t().bU.aO as ConcurrentLinkedQueue<c>).firstOrNull { it.A == p.player } ?: return
        val bgVar = bg()
        bgVar.b(welcomeMessage)
        bgVar.b(3)
        bgVar.a("RWPP")
        bgVar.a(null as c?)
        bgVar.c(-1)
        val a2 = bgVar.a(141)
        conn.a(a2)
    }
}