package com.github.hyuma.uhfr2000demo.model

import android.content.ContentProvider
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.github.hyuma.uhfr2000demo.lib.uhfr2000.FrameDecoder
import com.github.hyuma.uhfr2000demo.lib.uhfr2000.InventoryResponseFrame
import com.github.hyuma.uhfr2000demo.lib.uhfr2000.SerialCodec
import com.github.hyuma.uhfr2000demo.ui.MainActivity
import com.github.hyuma.uhfr2000demo.ui.not_connected.NotConnectedViewModel
import java.io.UnsupportedEncodingException

object UHFR2000 {
    val TAG: String = UHFR2000::class.java.simpleName
    val VENDOR_ID = 0x10C4
    val TAG_INVENTORY_INTERVAL = 200.toLong()

    var UHFR2000SerialDevice: UsbSerialDevice? = null
    var health = false
    val serialCodec = SerialCodec()
    val frameDecoder = FrameDecoder()

    fun setUsbSerialDevice(device: UsbSerialDevice){
        Log.d(MainActivity.TAG, "Seting device")
        UHFR2000SerialDevice = device
        if (!device.isOpen) {
            device.open()
        }
        device.setBaudRate(SerialCodec.BAUD_RATE)
        device.setDataBits(SerialCodec.DATA_BITS)
        device.setParity(UsbSerialInterface.PARITY_NONE)
        device.setStopBits(UsbSerialInterface.STOP_BITS_1)
        device.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
        device.read(mCallback)
        sendFrame(serialCodec.getReaderInformation())
    }

    var mCallback: UsbSerialInterface.UsbReadCallback = UsbSerialInterface.UsbReadCallback { byteArray ->
        //Log.d(TAG, "Getting Reply")
        //Defining a Callback which triggers whenever data is read.
        health = true
        try {
            frameDecoder.pushBytes(byteArray)
//            var logString = ""
//            byteArray.forEach {
//                logString += it.toUByte().toInt().toString()
//                logString += ","
//            }
//            Log.d(TAG, logString)
            frameDecoder.getFrames().forEach{frame->
                if(frame is InventoryResponseFrame){
                    val tagList = frame.getRFIDTags()
                    tagList.forEach{
                        Log.d(TAG, it.epc)
                    }
                }
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    fun isConnected():Boolean{
        sendFrame(serialCodec.getReaderInformation())
        return try{
            health && requireNotNull(UHFR2000SerialDevice).isOpen
        } catch (e: Exception) {
            false
        }
    }

    fun closeConnection(){
        requireNotNull(UHFR2000SerialDevice).close()
    }

    var tagMutableMap: MutableMap<String, Int> = mutableMapOf()
    var isContinueTagInventory = false

    fun startTagInventory(): LiveData<List<TagInventoryResult>> {
        val data = MutableLiveData<List<TagInventoryResult>>()
        tagMutableMap = mutableMapOf()
        isContinueTagInventory = true
        periodicTagInventoryExecution()
        return data
    }

    fun stopTagInventory(){
        isContinueTagInventory = false
    }

    private fun periodicTagInventoryExecution(){
        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                sendFrame(serialCodec.tagInventoryEPC(0))
                if(isContinueTagInventory)
                    handler.postDelayed(this, TAG_INVENTORY_INTERVAL)
            }
        }
        handler.post(r)
    }

    private fun sendFrame(frameByteArray: ByteArray): Boolean {
        if(UHFR2000SerialDevice != null){
            return if(!UHFR2000SerialDevice!!.isOpen){
                false
            }else{
                UHFR2000SerialDevice?.write(frameByteArray)
                true
            }
        } else {
            Log.d(TAG, "Device unavailable")
            return false
        }

    }

}