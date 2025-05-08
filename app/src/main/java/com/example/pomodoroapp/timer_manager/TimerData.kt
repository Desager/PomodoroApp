package com.example.pomodoroapp.timer_manager

data class TimerData(
    val id: Int,
    val totalS: Long,
    val currentS: Long = totalS,
    val isStarted: Boolean = false,
    val isFinished: Boolean = false
)
