package io.github.rwpp.config

val MasterSource
    get() = Source("Masterserver", "http://gs1.corrodinggames.com/masterserver/1.4/interface?action=list&game_version=176&game_version_beta=false;http://gs4.corrodinggames.net/masterserver/1.4/interface?action=list&game_version=176&game_version_beta=false")
data class Source(
    val name: String,
    val url: String
)