package com.github.hyuma.uhfr2000demo.lib.uhfr2000

import android.util.Log

class FrameDecoder {
    companion object {
        val TAG: String = FrameDecoder::class.java.simpleName
    }
    private var frameByteBuffer = ByteArray(0)

    fun pushBytes(byteArray: ByteArray){
        frameByteBuffer = frameByteBuffer.plus(byteArray)
    }

    fun getFrames(): ArrayList<ResponseFrame>{
        val frame_arraylist = ArrayList<ResponseFrame>()

        while(frameByteBuffer.isNotEmpty()){
            try{
                var resFrame = extractSingleFrameFIFO()
                frame_arraylist.add(resFrame!!)
            } catch (e: IncompleteFrameException) {
                // frame buffer is less than frame length (wait for next attempt)
                break
            } catch (e: CorruptedFrameException){
                // Reset buffer
                frameByteBuffer = ByteArray(0)
                break
            }
        }
        return frame_arraylist
    }

    private fun extractSingleFrameFIFO(): ResponseFrame{
        lateinit var resFrame: ResponseFrame

        // Decode Frame
        val len = frameByteBuffer[0].toInt()
        val cmd = frameByteBuffer[2].toInt()

        // Return null if frame buffer is less than frame length (wait for next attempt)
        if (frameByteBuffer.size < (len + 1))
            throw (IncompleteFrameException("Frame buffer size is less than frame length. Wait for next attempt."))

        // Raise Exception if CRC is wrong
        var singleFrameBytes = frameByteBuffer.sliceArray(0..len)

        if(cmd == SerialCodec.CMD_INVENTORY) {
            resFrame = InventoryResponseFrame(singleFrameBytes)
        } else {
            resFrame = ResponseFrame(singleFrameBytes)
        }



        if (!resFrame.isCRCValid()){
            singleFrameBytes.forEach {
                print(it.toInt())
                print(",")
            }
            println("CRC is unmached. Flushing Frame.")

            throw CorruptedFrameException()
        }

        // Delete bytes of decoded frames
        frameByteBuffer = frameByteBuffer.sliceArray(len+1..frameByteBuffer.lastIndex)

        return resFrame
    }

    class CorruptedFrameException(msg: String? = null): RuntimeException(msg)
    class IncompleteFrameException(msg: String? = null): RuntimeException(msg)
}