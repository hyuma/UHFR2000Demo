package com.github.hyuma.uhfr2000demo.lib.uhfr2000

import com.github.hyuma.uhfr2000demo.lib.uhfr2000.SerialCodec.*

class FrameDecoder {
    private var frameByteBuffer = ByteArray(0)

    fun pushBytes(byteArray: ByteArray){
        frameByteBuffer = frameByteBuffer.plus(byteArray)
    }
    fun getFrames(): ArrayList<ResponseFrame>{
        val frame_arraylist = ArrayList<ResponseFrame>()

        while(frameByteBuffer.isNotEmpty()){
            try{
                var res_frame = extractSingleFrameFIFO()
                frame_arraylist.add(res_frame!!)
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
        // Decode Frame
        val len = frameByteBuffer[0].toInt()
        val cmd = frameByteBuffer[3].toInt()

        // Return null if frame buffer is less than frame length (wait for next attempt)
        if (frameByteBuffer.size < (len + 1))
            throw (IncompleteFrameException("Frame buffer size is less than frame length. Wait for next attempt."))

        // Raise Exception if CRC is wrong
        var singleFrameBytes = frameByteBuffer.sliceArray(0..len)
        val res_frame = ResponseFrame(singleFrameBytes)
        if (!res_frame.isCRCValid()){
            singleFrameBytes.forEach {
                print(it.toInt())
                print(",")
            }
            println("CRC is unmached. Flushing Frame.")

            throw CorruptedFrameException()
        }

        // Delete bytes of decoded frames
        frameByteBuffer = frameByteBuffer.sliceArray(len+1..frameByteBuffer.lastIndex)

        return res_frame
    }

    class CorruptedFrameException(msg: String? = null): RuntimeException(msg)
    class IncompleteFrameException(msg: String? = null): RuntimeException(msg)
}