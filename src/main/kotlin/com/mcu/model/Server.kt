package com.mcu.model

import java.util.*

class Server {
    var name : String? = null
    var aws : Aws = Aws()
    var minecraft : Minecraft = Minecraft()
}

class Aws {
    var id : String? = null
    var online : Boolean = false
    var start : Date? = null
    var update : Date? = null
}

class Minecraft {
    var online : Boolean = false
    var now : Int = 0
    var max : Int = 0
    var start : Date? = null
    var update : Date? = null
}