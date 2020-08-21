package com.github.hyuma.uhfr2000demo.ui.tag_inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.github.hyuma.uhfr2000demo.UHFR2000

class TagInventoryViewModel(application: Application): AndroidViewModel(application) {

    fun onClickStartBtn() {
        UHFR2000.startInventory()
    }
    fun onClickStopBtn() {
        UHFR2000.stopInventory()
    }
}