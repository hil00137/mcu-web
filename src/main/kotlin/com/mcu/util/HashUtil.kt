package com.mcu.util

import java.security.Key
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

class HashUtil {
    companion object {
        private var keySpec : Key? = null
        private var iv : String? = null
        private var base64Encoder : Base64.Encoder? = null
        private var base64Decorder : Base64.Decoder? = null
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

        fun encryptAES256(str : String): String {
            if (keySpec == null) {
                initAES256()
            }
            val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
            c.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv!!.toByteArray()))
            val encrypted = c.doFinal(str.toByteArray(Charsets.UTF_8))
            return base64Encoder!!.encodeToString(encrypted).toString()
        }

        fun decrpytAES256(str: String): String {
            if (keySpec == null) {
                initAES256()
            }
            val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
            c.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv!!.toByteArray()))
            val byteStr = try {
                base64Decorder!!.decode(str)
            } catch (exception : Exception) {
                "".toByteArray(Charsets.UTF_8)
            }
            return c.doFinal(byteStr).toString(Charsets.UTF_8)
        }

        fun initAES256() {
            base64Encoder = Base64.getMimeEncoder()
            base64Decorder = Base64.getMimeDecoder()
            val key = "dlrjtdmsdkagdlqslek"
            iv = key.substring(0,16)
            val keyBytes = ByteArray(16)
            val b = key.toByteArray(Charsets.UTF_8)
            var len = b.size
            if (len > keyBytes.size) {
                len = keyBytes.size
            }
            System.arraycopy(b, 0, keyBytes, 0, len)
            keySpec = SecretKeySpec(keyBytes, "AES")
        }
    }
}