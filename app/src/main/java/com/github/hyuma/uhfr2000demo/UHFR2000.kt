package com.github.hyuma.uhfr2000demo

import androidx.lifecycle.LiveData
import com.github.hyuma.uhfr2000demo.lib.*
import androidx.lifecycle.MutableLiveData
import androidx.databinding.ObservableField
import android.os.Handler
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import java.io.UnsupportedEncodingException

object UHFR2000 {
    val TAG: String = UHFR2000::class.java.simpleName
    val VENDOR_ID = 0x10C4
    val TAG_INVENTORY_INTERVAL = 300.toLong()

    private var UHFR2000SerialDevice: UsbSerialDevice? = null
    private var health = false
    private val serialCodec = SerialCodec()
    private val frameDecoder = FrameDecoder()
    var bufferRFIDTagMutableList = mutableListOf<BufferRFIDTag>()
    var isReadBufferCompleted = ObservableField<Boolean>()

    fun setUsbSerialDevice(device: UsbSerialDevice){
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

    private var mCallback: UsbSerialInterface.UsbReadCallback = UsbSerialInterface.UsbReadCallback { byteArray ->
        //Defining a Callback which triggers whenever data is read.
        health = true
        try {
            frameDecoder.pushBytes(byteArray)
            frameDecoder.getFrames().forEach{frame->
                if(frame is InventoryResponseFrame){
                    val tagList = frame.getRFIDTags()
                    tagList.forEach{
                        Log.d(TAG, it.code)
                    }
                } else if (frame is ReadBufferResponseFrame) {
                    // Response of Read Buffer
                    val tagList = frame.getBufferRFIDTags()

                    tagList.forEach{
                        bufferRFIDTagMutableList.add(it)
//                        Log.d(TAG, it.code + ": " + it.count.toString())
                    }
                    isReadBufferCompleted.set(frame.isFinalFrame())
                }
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {

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


    // ------------------------------
    // SEND COMMANDS
    // ------------------------------
    private var tagMutableMap: MutableMap<String, Int> = mutableMapOf()
    private var isContinueInventory = false


    // Tag Inventory
    fun startInventory(): LiveData<List<TagInventoryResult>> {
        val data = MutableLiveData<List<TagInventoryResult>>()
        tagMutableMap = mutableMapOf()
        isContinueInventory = true
        periodicInventoryExecution()
        return data
    }

    fun stopInventory(){
        isContinueInventory = false
    }

    private fun periodicInventoryExecution(){
        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                sendFrame(serialCodec.getInventoryEPCFrame(0))
                if(isContinueInventory)
                    handler.postDelayed(this, TAG_INVENTORY_INTERVAL)
            }
        }
        handler.post(r)
    }

    // Buffer Tag Inventory: TID
    fun startBufferInventoryTID() {
        isContinueInventory = true
        periodicBufferInventoryTIDExecution()
    }

    private fun periodicBufferInventoryTIDExecution(){
        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                sendFrame(serialCodec.getInventoryTIDWithBufferFrame(0))
                if(isContinueInventory)
                    handler.postDelayed(this, TAG_INVENTORY_INTERVAL)
            }
        }
        handler.post(r)
    }

    // Buffer Tag Inventory: EPC
    fun startBufferInventoryEPC() {
        isContinueInventory = true
        periodicBufferInventoryEPCExecution()
    }

    private fun periodicBufferInventoryEPCExecution(){
        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                sendFrame(serialCodec.getInventoryEPCWithBufferFrame(0))
                if(isContinueInventory)
                    handler.postDelayed(this, TAG_INVENTORY_INTERVAL)
            }
        }
        handler.post(r)
    }

    // Read Buffer
    fun readBuffer(){
        // Reset mutableLiveData
        isReadBufferCompleted.set(false)
        println(isReadBufferCompleted.get())
        bufferRFIDTagMutableList = mutableListOf()
        sendFrame(serialCodec.getReadBufferFrame(0))
    }

    // Clear Buffer
    fun clearBuffer(){
        sendFrame(serialCodec.getClearBufferFrame(0))
    }

    // ------------------------------
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