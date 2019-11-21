package com.github.hyuma.uhfr2000demo.lib.uhfr2000

open class ResponseFrame (frameBytes: ByteArray){
    var len: Int = frameBytes[0].toUByte().toInt()
    var adr: Int = frameBytes[1].toUByte().toInt()
    var cmd : Int = frameBytes[2].toUByte().toInt()
    var status : Int = frameBytes[3].toUByte().toInt()
    var data :ByteArray = ByteArray(0)
    var lsb_crc16 : Int = frameBytes[len - 1].toUByte().toInt()
    var msb_crc16 : Int = frameBytes[len].toUByte().toInt()
    init {
        for (i in 4..(len - 2)) {
            data = data.plus(frameBytes[i])
        }
    }

    override fun toString(): String {
        var myString = len.toString() + ", " + adr.toString() + ", " +
                cmd.toString() + ", " + status.toString() + ", "
        data.forEach {
            myString += (it.toString() + ", ")
        }
        myString += lsb_crc16.toString()
        myString += msb_crc16.toString()
        return myString
    }

    fun isCRCValid(): Boolean{
        var calculatedCrc = 0
        calculatedCrc = if (data == null) {
            CCITT_CRC16.generate(byteArrayOf(len.toByte(), adr.toByte(), cmd.toByte(), status.toByte()))
        } else {
            CCITT_CRC16.generate(byteArrayOf(len.toByte(), adr.toByte(), cmd.toByte(), status.toByte()).plus(data))
        }

        val calculated_lsb_crc = calculatedCrc and 0xFF
        val calculated_msb_crc = calculatedCrc ushr 8
        return (calculated_lsb_crc.toByte() == lsb_crc16.toByte()) and (calculated_msb_crc.toByte() == msb_crc16.toByte())
    }

}