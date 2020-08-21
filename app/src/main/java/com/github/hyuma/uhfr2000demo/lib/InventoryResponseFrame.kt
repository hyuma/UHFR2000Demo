package com.github.hyuma.uhfr2000demo.lib

import android.util.Log
import java.util.*

class InventoryResponseFrame(frameBytes: ByteArray): ResponseFrame(frameBytes) {
    companion object{
        val TAG: String = InventoryResponseFrame::class.java.simpleName
    }

    @ExperimentalUnsignedTypes
    fun getRFIDTags():List<RFIDTag>{
        var tagList = mutableListOf<RFIDTag>()
        val tagCount = super.data[1].toUByte().toInt()
        if(tagCount == 0)
            return tagList

        var tagListByteArray = data.sliceArray(2..data.lastIndex)
        for(i in 1..tagCount){
            var epcString = ""
            val dataLength = tagListByteArray[0].toUByte().toInt()
            // Exclude bit 7 of dataLength byte
            val epcByteArray = tagListByteArray.sliceArray(1..dataLength)
            val rssi = tagListByteArray[dataLength+1].toUByte().toInt()
            epcByteArray.forEach {
                epcString = epcString + it.toUByte().toInt().toString(16)
            }
            tagList.add(RFIDTag(epcString.toUpperCase(Locale.ROOT), rssi))

            tagListByteArray = tagListByteArray
                .sliceArray(dataLength+1..tagListByteArray.lastIndex)
        }
        return tagList
    }
}