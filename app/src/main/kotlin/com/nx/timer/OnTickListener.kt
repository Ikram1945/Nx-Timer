package com.nx.timer

interface OnTickListener {
    fun onTick(elapsedMillis: Long, totalMillis: Long)
}