package com.github.hyuma.uhfr2000demo.ui.not_connected

import android.app.Application
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.hyuma.uhfr2000demo.UHFR2000

class NotConnectedViewModel(application: Application): AndroidViewModel(application) {
    companion object {
        val TAG: String = NotConnectedViewModel::class.java.simpleName
        private const val CONNECTION_CHECK_INTERVAL:Long = 3000
    }

    val navigateToTagInventoryFragment = MutableLiveData<Boolean>()
    val connectUHFR2000 = MutableLiveData<Boolean>()

    init {
        startPeriodicConnectionCheck()
    }

    private fun startPeriodicConnectionCheck(){
        val handler = Handler()
        val r = object : Runnable {
            override fun run() {
                Log.d(TAG, "Connection Check Now")
                if (UHFR2000.isConnected()) {
                    navigateToTagInventoryFragment.value = true
                    return
                }
                Log.d(TAG, "not connected")
                handler.postDelayed(this, CONNECTION_CHECK_INTERVAL)
            }
        }
        handler.post(r)
    }

    fun onClickConnectButton() {
        connectUHFR2000.value = true
    }

    class Factory(private val application: Application): ViewModelProvider.NewInstanceFactory(){
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotConnectedViewModel(
                application
            ) as T
        }
    }
}