package com.github.hyuma.uhfr2000demo.framework.usb

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.github.hyuma.uhfr2000demo.lib.uhfr2000.SerialCodec
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

class UHFR2000ConnectionService: Service() {

    var UHFR2000SerialDevice: UsbSerialDevice? = null
    var health = false

    companion object {
        val TAG: String = this::class.java.simpleName
        val VENDOR_ID = 0x10C4
    }

    inner class UHFR2000Binder : Binder() {
        fun setUHFR2000Device(device:UsbSerialDevice){
            UHFR2000SerialDevice = device
            if (!device.isOpen) {
                device.open()
            }
            device.setBaudRate(SerialCodec.BAUD_RATE)
            device.setDataBits(SerialCodec.DATA_BITS)
            device.setParity(UsbSerialInterface.PARITY_NONE)
            device.setStopBits(UsbSerialInterface.STOP_BITS_1)
            device.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
//            arduinoSerial.read(mCallback)
//            sendSignal("s")
        }

        fun isConnected():Boolean{
            return try{
                health && requireNotNull(UHFR2000SerialDevice).isOpen
            } catch (e: Exception) {
                false
            }
        }
    }

    private val binder = UHFR2000Binder()
    override fun onBind(intent: Intent): IBinder? {
        // Return this instance of LocalService so clients can call public methods
        Log.d(TAG, "Bind")
        return binder
    }
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "ReBind")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "UnBind")
        try {
            requireNotNull(UHFR2000SerialDevice).close()
        } finally {
            return true
        }
    }
}