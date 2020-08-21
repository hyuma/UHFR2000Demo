package com.github.hyuma.uhfr2000demo.ui.tag_inventory

import android.app.Application
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.github.hyuma.uhfr2000demo.UHFR2000
import com.github.hyuma.uhfr2000demo.lib.BufferRFIDTag

class TagInventoryViewModel(application: Application): AndroidViewModel(application) {

    val bufferDataInvTIDResult = ObservableField<List<BufferRFIDTag>>()

    fun onClickStartBtn() {
        UHFR2000.clearBuffer()
        UHFR2000.startBufferInventoryEPC()
    }
    fun onClickStopBtn() {
        UHFR2000.stopInventory()
    }

    fun onClickReadBufferBtn() {
        UHFR2000.isReadBufferCompleted.addOnPropertyChangedCallback(
            object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    if( UHFR2000.isReadBufferCompleted.get()!! ) {
                        println("I'm ViewModel. Received ReadBufferCompleted ")
                        UHFR2000.isReadBufferCompleted.removeOnPropertyChangedCallback(this)
                        bufferDataInvTIDResult.set(UHFR2000.bufferRFIDTagMutableList)

                        println("Tag Count: "+UHFR2000.bufferRFIDTagMutableList.size.toString())
                        UHFR2000.bufferRFIDTagMutableList.forEach {
                            println(it.code)
                        }
                    } else {
                        println("I'm ViewModel. it was false ")
                    }
                }
            }
        )
        UHFR2000.readBuffer()

    }
}