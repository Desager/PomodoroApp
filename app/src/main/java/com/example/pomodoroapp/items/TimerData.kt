package com.example.pomodoroapp.items

data class TimerData(
    val id: Int,
    val totalS: Long,
    var currentS: Long = totalS,
    var isStarted: Boolean = false
)
