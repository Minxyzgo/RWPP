package io.github.rwpp.game.config

interface ConfigHandler {
    fun <T> getConfig(name: String): T

    fun setConfig(name: String, value: Any?)

    fun saveConfig()
}