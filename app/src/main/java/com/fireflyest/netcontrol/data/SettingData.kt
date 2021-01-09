package com.fireflyest.netcontrol.data

class SettingData private constructor(){

    companion object {
        val instance = SettingData()
    }

    var notifyHex: Boolean = false

    var themeNight: Boolean = false

}