package com.mcu.util

import java.security.MessageDigest
import kotlin.experimental.and

class HashUtil {
    companion object {
        fun sha512(password : String) : String {
            if (password == "") return ""

            val salt = "revomin"

            val md = MessageDigest.getInstance("SHA-512")
            val digest = md.digest((password+salt).toByteArray())
            val sb = StringBuilder()
            for (i in digest) {
                sb.append(((i and 0xff.toByte()) + 0x100).toString(16).substring(1))
            }

            return sb.toString()
        }
    }
}