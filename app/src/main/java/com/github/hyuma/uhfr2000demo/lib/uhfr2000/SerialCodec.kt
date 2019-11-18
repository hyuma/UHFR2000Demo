package com.github.hyuma.uhfr2000demo.lib.uhfr2000

class SerialCodec {
    companion object {
        const val BAUD_RATE = 57600
        const val DATA_BITS = 8

        const val CMD_INVENTORY = 0x1
        const val CMD_GET_READER_INFO = 0x21
        const val CMD_OBTAIN_GPIO_STATE = 0x47
    }

    fun tagInventoryEPC(adr:Int):ByteArray{
        val data = byteArrayOf(0b0100.toByte(), 0xFF.toByte())
        var frame = RequestFrame(adr, CMD_INVENTORY, data)
        return frame.getByteArray()
    }

    fun tagInventoryTID(adr:Int):ByteArray{
        val data = byteArrayOf(0b0100.toByte(), 0xFF.toByte())
        var frame = RequestFrame(adr, CMD_INVENTORY, data)
        return frame.getByteArray()
    }

    fun tagInventoryFastID(adr:Int):ByteArray{
        val data = byteArrayOf(0b00100100.toByte(), 0xFF.toByte())
        var frame = RequestFrame(adr, CMD_INVENTORY, data)
        return frame.getByteArray()
    }

    fun getReaderInformation(): ByteArray{
        var frame = RequestFrame(255, CMD_GET_READER_INFO, null)
        return frame.getByteArray()
    }

    fun obtainGPIOState(adr:Int): ByteArray{
        var frame = RequestFrame(adr, CMD_OBTAIN_GPIO_STATE, null)
        return frame.getByteArray()
    }


    class RequestFrame (val adr: Int = 0, val cmd: Int = 0, val data: ByteArray? = null){
        var lsb_crc = 0
        var msb_crc = 0
        val len get() = calcLen()

        init {
            var crc = 0
            if (data == null) {
                crc = CCITT_CRC16.generate(byteArrayOf(len.toByte(), adr.toByte(), cmd.toByte()))
            } else {
                crc = CCITT_CRC16.generate(byteArrayOf(len.toByte(), adr.toByte(), cmd.toByte()).plus(data))
            }
            lsb_crc = crc and 0xFF
            msb_crc = crc ushr 8

        }

        private fun calcLen(): Int {
            return if (data != null) {
                4 + data!!.size
            }else{
                4
            }
        }

        fun getByteArray() : ByteArray{
            return if (data != null) {
                byteArrayOf(len.toByte(), adr.toByte(), cmd.toByte()).plus(data!!).plus(byteArrayOf(lsb_crc.toByte(), msb_crc.toByte()))
            } else {
                byteArrayOf(len.toByte(), adr.toByte(), cmd.toByte(), lsb_crc.toByte(), msb_crc.toByte())
            }
        }
    }


    open class ResponseFrame (frameBytes: ByteArray){
        var len: Int = frameBytes[0].toInt()
        var adr: Int = frameBytes[1].toInt()
        var cmd : Int = frameBytes[2].toInt()
        var status : Int = frameBytes[3].toInt()
        var data :ByteArray = ByteArray(0)
        var lsb_crc16 : Int = frameBytes[len - 1].toInt()
        var msb_crc16 : Int = frameBytes[len].toInt()
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
}