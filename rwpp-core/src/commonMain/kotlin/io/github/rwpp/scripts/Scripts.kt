/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.scripts

import io.github.rwpp.*
import io.github.rwpp.config.ConfigIO
import io.github.rwpp.core.Initialization
import io.github.rwpp.event.Event
import io.github.rwpp.event.EventPriority
import io.github.rwpp.event.GlobalEventChannel
import io.github.rwpp.external.ExternalHandler
import io.github.rwpp.game.Game
import io.github.rwpp.game.audio.GameSoundPool
import io.github.rwpp.game.mod.ModManager
import io.github.rwpp.net.Client
import io.github.rwpp.net.Net
import io.github.rwpp.net.Packet
import io.github.rwpp.platform.Platform
import io.github.rwpp.ui.parseColorToArgb
import party.iroiro.luajava.ClassPathLoader.BufferOutputStream
import party.iroiro.luajava.lua54.Lua54
import party.iroiro.luajava.value.RefLuaValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.timer
import kotlin.math.roundToInt

@Suppress("MemberVisibilityCanBePrivate")
object Scripts : Initialization {
    val lua = Lua54()

    override fun init() {
        try {
            lua.setExternalLoader { module, _ ->
                val extension = appKoin.get<ExternalHandler>()
                    .getAllExtensions()
                    .getOrThrow()
                    .firstOrNull { it.config.id == module.split("/").first() }
                    ?: throw IllegalArgumentException("Extension not found: $module")


                val path = module.removePrefix(extension.config.id + "scripts/")

                val inputStream = extension.zipFile?.let { zip ->
                    val entry = zip.getEntry(path)
                    zip.getInputStream(entry)
                } ?: File(extension.file, path).inputStream()

                inputStream.use { input ->
                    val output = ByteArrayOutputStream()
                    val bytes = ByteArray(4096)
                    var i: Int
                    do {
                        i = input.read(bytes)
                        if (i != -1) {
                            output.write(bytes, 0, i)
                        }
                    } while (i != -1)
                    val buffer = ByteBuffer.allocateDirect(output.size())
                    output.writeTo(BufferOutputStream(buffer))
                    buffer.flip()
                    buffer
                }
            }
        } catch (e: Exception) {
            logger.error(e.stackTraceToString())
        }

        lua["scripts"] = lua
        lua["game"] = appKoin.get<Game>()
        lua["externalHandler"] = appKoin.get<ExternalHandler>()
        lua["soundPool"] = appKoin.get<GameSoundPool>()
        lua["net"] = appKoin.get<Net>()
        lua["modManager"] = appKoin.get<ModManager>()
        lua["config"] = appKoin.get<ConfigIO>()
        lua["commands"] = commands
        lua["isAndroid"] = Platform.isAndroid()
        lua["isDesktop"] = Platform.isDesktop()
        lua["version"] = projectVersion

        @Suppress("UNCHECKED_CAST")
        lua.register("filterEvents") { _, args ->
            GlobalEventChannel.subscribeGlobalAlways(
                Class.forName(
                    "io.github.rwpp.event.events." + args[0].toJavaObject() as String
                ) as Class<Event>, priority = if (args.size > 2)
                    EventPriority.valueOf(args[1].toJavaObject() as String)
                else EventPriority.NORMAL
            ) {
                synchronized(lua.mainState) {
                    (args[if (args.size > 2) 2 else 1] as RefLuaValue).call(it)
                }
            }

            arrayOf()
        }

        @Suppress("UNCHECKED_CAST")
        lua.register("filterEventsOnce") { _, args ->
            GlobalEventChannel.subscribeGlobalOnce(
                Class.forName(
                    "io.github.rwpp.event.events." + args[0].toJavaObject() as String
                ) as Class<Event>, priority = if (args.size > 2)
                    EventPriority.valueOf(args[1].toJavaObject() as String)
                else EventPriority.NORMAL
            ) {
                synchronized(lua.mainState) {
                    args[if (args.size > 2) 2 else 1].call(it)
                }
            }

            arrayOf()
        }

        lua.register("registerCommand") { _, args ->
            commands.register<Any?>(
                args[0].toJavaObject() as String,
                args[1].toJavaObject() as String,
                args[2].toJavaObject() as String?,
            ) { a, p -> if (p != null) {
                synchronized(lua.mainState) {
                    args[3].call(a, p)
                }
            } }

            arrayOf()
        }

        lua.register("onExit") { _, args ->
            appKoin.get<AppContext>().onExit {
                args[0].call()
            }

            arrayOf()
        }

        @Suppress("UNCHECKED_CAST")
        lua.register("registerPacketListener") { _, args ->
            val packetType = args[0].toInteger().toInt()
            appKoin.get<Net>().listeners.getOrPut(packetType) { mutableListOf() }.add(
                args[1].toProxy(Function2::class.java) as (Client, Packet) -> Boolean
            )
            arrayOf()
        }

        //UI
        lua.register("color") { l, args ->
            l.pushJavaObject(LuaColor(
                parseColorToArgb(args[0].toJavaObject() as String)
            ))
            arrayOf(l.get())
        }

        lua.register("timer")  { l, args ->
            l.pushJavaObject(timer(
                initialDelay = args[0].toNumber().toLong(),
                period = args[1].toNumber().toLong()) {
                synchronized(lua.mainState) {
                    args[2].call()
                }
            })
            arrayOf(l.get())
        }

        lua.register("text") { l, args ->
            l.pushJavaObject(LuaWidget.LuaText(
                args[0].toJavaObject() as String,
                args[1].toNumber().roundToInt(),
                args[2].toJavaObject() as LuaColor,
            ))
            arrayOf(l.get())
        }

        @Suppress("UNCHECKED_CAST")
        lua.register("dropdown") { l, args ->
            l.pushJavaObject(LuaWidget.LuaDropdown(
                (args[0].toJavaObject() as HashMap<*, String>).values.toTypedArray(), args[1].toJavaObject() as String,
                { args[2].call().first().toJavaObject() as String },
                { index, value ->
                    synchronized(lua.mainState) {
                        args[3].call(index, value)
                    }
                }
            ))
            arrayOf(l.get())
        }

        lua.register("textField") { l, args ->
            l.pushJavaObject(LuaWidget.LuaTextField(
                args[0].toJavaObject() as String,
                {
                    synchronized(lua.mainState) {
                        args[1].call().first().toJavaObject() as String
                    }
                },
                { str ->
                    synchronized(lua.mainState) {
                        args[2].call(str)
                    }
                }
            ))
            arrayOf(l.get())
        }

        lua.register("checkbox") { l, args ->
            l.pushJavaObject(LuaWidget.LuaCheckbox(
                args[0].toJavaObject() as String,
                {
                    synchronized(lua.mainState) {
                        args[1].call().first().toJavaObject() as Boolean
                    }
                },
                { bool ->
                    synchronized(lua.mainState) {
                        args[2].call(bool)
                    }
                }
            ))
            arrayOf(l.get())
        }

        lua.register("textButton") { l, args ->
            l.pushJavaObject(LuaWidget.LuaTextButton(
                args[0].toJavaObject() as String
            ) {
                synchronized(lua.mainState) {
                    args[1].call()
                }
            })
            arrayOf(l.get())
        }
    }

    fun loadScript(id: String, src: String) {
        try {
            val body = """
                local id = '$id' 
                local extension = externalHandler:getExtensionById(id)
                local info = function(msg) 
                    local logger = java.method(java.import('io.github.rwpp.GlobalKt'), 'getLogger')() 
                    logger:info('[' .. id .. '] ' .. msg) 
                end
                local getConfig = function(key) 
                    return config:readSingleConfig(id, key)
                end
                local setConfig = function(key, value) 
                    config:saveSingleConfig(id, key, value)
                end
                local broadcast = function(eventClass, ...) 
                    java.import('io.github.rwpp.event.EventKt'):broadcastIn(java.import('io.github.rwpp.event.events.' .. eventClass)(...))
                end
                local reply = function(player, message, title, color)
                     local gameRoom = game:getGameRoom()
                     gameRoom:sendMessageToPlayer(player, title or id, message, color or -1)
                end
                local functionN = function(count, func) 
                    return java.proxy('kotlin.jvm.functions.Function' .. count, func)
                end
            """.trimIndent().replace("\n", " ")
            lua.run("$body $src")
        } catch (e: Exception) {
            logger.error(e.stackTraceToString())
        }
    }
}