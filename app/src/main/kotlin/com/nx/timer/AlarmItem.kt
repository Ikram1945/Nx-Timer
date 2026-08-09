package com.nx.timer

data class AlarmItem(
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDays: List<Int>
)