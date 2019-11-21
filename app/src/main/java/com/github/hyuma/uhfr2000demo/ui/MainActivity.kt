package com.github.hyuma.uhfr2000demo.ui

import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.felhr.usbserial.UsbSerialDevice
import com.github.hyuma.uhfr2000demo.R
import com.github.hyuma.uhfr2000demo.model.UHFR2000
import com.github.hyuma.uhfr2000demo.ui.not_connected.NotConnectedFragment

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        private const val ACTION_USB_PERMISSION = "com.github.hyuma.USB_PERMISSION"
    }
    private lateinit var usbManager: UsbManager


    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        val apply = device?.apply {
                            //set up device communication
                            UHFR2000.setUsbSerialDevice(connectDevice(device))
                        }
                        apply
                    } else {
                        Log.d(TAG, "permission denied for device $device")
                    }
                }
            }
        }
    }

    private fun connectDevice(device: UsbDevice): UsbSerialDevice {
        val usbConnection: UsbDeviceConnection = usbManager.openDevice(device)
        Log.d(TAG,"USB Device Connected")
        Toast.makeText(this, "USB Device connected", Toast.LENGTH_SHORT).show()
        return UsbSerialDevice.createUsbSerialDevice(device, usbConnection)
    }

    fun setUpUSBSerialConnection(){
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDevices: HashMap<String, UsbDevice> = usbManager.deviceList
        if (usbDevices.isNotEmpty()) {
            for ((_, device) in usbDevices) {
                // break if already connected
                Log.d(TAG, device.vendorId.toString())
                if (UHFR2000.isConnected()) {
                    Log.d(TAG, "End setting up USB Serial Connection. Already connected.")
                    return
                }

                val vendorId = device.vendorId
                if (vendorId == UHFR2000.VENDOR_ID)
                {
                    val pi = PendingIntent.getBroadcast(
                        this, 0,
                        Intent(ACTION_USB_PERMISSION), 0
                    )
                    val filter = IntentFilter(ACTION_USB_PERMISSION)
                    registerReceiver(usbReceiver, filter)
                    usbManager.requestPermission(device, pi)
                    Log.d(TAG, "Found UHFR2000")
                    Toast.makeText(this, "Found UHFR2000: " + device.vendorId.toString() + ", "+ device.deviceId.toString(), Toast.LENGTH_SHORT).show()
                    return
                }
            }
            Log.d(TAG, "No UHFR2000 Device Found")
            Toast.makeText(this, "No UHFR2000 Device Found", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "No USB Device connected")
            Toast.makeText(this, "No USB device connected", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setUpUSBSerialConnection()
        if (savedInstanceState == null) {
            val fragment = NotConnectedFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container,
                    fragment,
                    NotConnectedFragment.TAG
                )
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
