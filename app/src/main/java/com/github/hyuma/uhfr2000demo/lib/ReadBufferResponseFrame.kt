package com.github.hyuma.uhfr2000demo.lib

import android.util.Log
import java.util.*

class ReadBufferResponseFrame(frameBytes: ByteArray): ResponseFrame(frameBytes) {
    companion object{
        val TAG: String = ReadBufferResponseFrame::class.java.simpleName
    }

    fun isFinalFrame():Boolean{
        return (this.status == 0x01)
    }

    @ExperimentalUnsignedTypes
    fun getBufferRFIDTags():List<BufferRFIDTag>{
        var tagList = mutableListOf<BufferRFIDTag>()
        val tagCount = super.data[0].toUByte().toInt()
        if(tagCount == 0)
            return tagList

        var tagListByteArray = data.sliceArray(1..data.lastIndex)
        for(i in 1..tagCount){
            // For each Tag
            var codeString = ""
            val antenna = tagListByteArray[0].toUByte().toInt()
            val codeLength = tagListByteArray[1].toUByte().toInt()
            // Exclude bit 7 of dataLength byte
            val codeByteArray = tagListByteArray.sliceArray(2..codeLength+1)
            val rssi = tagListByteArray[codeLength+1].toUByte().toInt()
            val count = tagListByteArray[codeLength+2].toUByte().toInt()
            codeByteArray.forEach {
                codeString += String.format("%02x", it.toUByte().toInt())
            }
            tagList.add(BufferRFIDTag(codeString.toUpperCase(Locale.ROOT),antenna, rssi, count))

            tagListByteArray = tagListByteArray
                .sliceArray(codeLength+4..tagListByteArray.lastIndex)
        }
        return tagList
    }
}