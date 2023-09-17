package io.github.rwpp.net

data class RoomDescription(
    val uuid: String,
    val roomOwner: String, // ? for official server and custom client is always 'Unnamed'
    val gameVersion: Int,
    val netWorkAddress: String,
    val localAddress: String,
    val port: Long,
    val isOpen: Boolean,
    val creator: String,
    val requiredPassword: Boolean,
    val mapName: String,
    val mapType: String,
    val roomType: String,
    val version: String,
    val isLocal: Boolean,
    val displayMapName: String, // not sure, source code doesn't use this
    val playerCurrentCount: Int?, // may be blank
    val playerMaxCount: Int?,
    val isUpperCase: Boolean, // ???
    val uuid2: String, // use to get real ip from list??
    val unknown: Boolean, // it is unused in source code
    val mods: String, // even though, this cannot be evidence that the mod has been enabled
    val roomId: Int,
) {
    fun addressProvider(): String {
        if (this.roomId == 0) {
            return "$netWorkAddress:$port"
        }
        return "get|" + uuid2.replace("|", ".") + "|" + roomId + "|" + requiredPassword + "|" + port

    }
}

val List<RoomDescription>.sorted
    get() = this.sortedBy {
        when {
            it.isUpperCase && it.netWorkAddress.startsWith("url:") -> 0
            //it.isUpperCase -> 1
            it.isLocal -> 2
            it.roomType.contains("battleroom") -> {
                if(it.playerCurrentCount != null && it.playerMaxCount != null && it.playerCurrentCount < it.playerMaxCount) {
                    if(it.isUpperCase) {
                        if(it.playerCurrentCount != 0) 3 else 4
                    } else if(it.isOpen) 7 else 9
                } else if(it.isUpperCase) 6
                else if(it.isOpen) 8 else 9
            }
            else -> 99
        }
    }