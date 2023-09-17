package io.github.rwpp.game.mod

interface Mod {
    val id: Int
    val name: String
    val description: String
    val minVersion: String
    var isEnabled: Boolean
}