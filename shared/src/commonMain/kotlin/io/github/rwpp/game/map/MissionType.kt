package io.github.rwpp.game.map

sealed class MissionType {
    data object Default : MissionType() {
        override fun toString(): String = "Default"
    }

    //not implemented
    class ModType(val mod: String) : MissionType()
}