package com.github.hyuma.uhfr2000demo.lib


class CCITT_CRC16 {
    companion object {
        fun generate(byteArray: ByteArray): Int {
            val POLYNOMIAL = 0x8408
            var current_crc_value = 0xFFFF
            for (i in byteArray) {
                current_crc_value = current_crc_value xor (i.toInt() and 0xFF)
                for (j in 0..7) {
                    if (current_crc_value and 0x0001 != 0) {
                        current_crc_value = current_crc_value.ushr(1) xor POLYNOMIAL
                    } else {
                        current_crc_value = current_crc_value.ushr(1)
                    }
                }
            }
            return (current_crc_value and 0xFFFF)
        }
    }
}
