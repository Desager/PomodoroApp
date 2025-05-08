package com.example.pomodoroapp.timer_manager

interface TimerManagerListener {

    fun onStop() {}

    fun onTick(timerData: TimerData) {}

    fun onFinish() {}

    fun onDelete() {}
}