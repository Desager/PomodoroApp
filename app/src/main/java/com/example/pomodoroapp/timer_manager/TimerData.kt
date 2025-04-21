package com.example.pomodoroapp.timer_manager

data class TimerData(
    val id: Int,
    val totalS: Long,
    var currentS: Long = totalS,
    var isStarted: Boolean = false,
    var isFinished: Boolean = false
)
