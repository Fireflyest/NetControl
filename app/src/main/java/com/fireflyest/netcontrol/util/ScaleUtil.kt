package com.fireflyest.netcontrol.util

import java.lang.Long.parseLong
import java.util.*

object ScaleUtil {

    /** * 将16进制的字符串转换为字节数组 *
     * @param message * @return 字节数组
     */
    fun getHexBytes(message: String): ByteArray {
        val len = message.length / 2
        val chars = message.toCharArray()
        val hexStr = arrayOfNulls<String>(len)
        val bytes = ByteArray(len)
        var i = 0
        var j = 0
        while (j < len) {
            hexStr[j] = "" + chars[i] + chars[i + 1]
            bytes[j] = hexStr[j]!!.toInt(16).toByte()
            i += 2
            j++
        }
        return bytes
    }

    /**
     * 将byte数组转为16进制字符串 方便显示
     */
    fun bytesToHexString(src: ByteArray): String {
        val result = StringBuilder()
        for (b in src) {
            var hexString = Integer.toHexString(b.toInt() and 0xFF)
            if (hexString.length == 1) {
                hexString = "0$hexString"
            }
            result.append(hexString.toUpperCase(Locale.ROOT))
        }
        return result.toString()
    }

}