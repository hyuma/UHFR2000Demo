package com.github.hyuma.uhfr2000demo.model

data class TagInventoryResult(
    val epc: String,
    var count: Int = 0,
    var rssi: Int = 0
)