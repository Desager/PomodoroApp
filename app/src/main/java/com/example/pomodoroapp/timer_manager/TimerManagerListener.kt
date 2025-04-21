package com.example.pomodoroapp.timer_manager

interface TimerManagerListener {

    fun onStart() {}

    fun onStop() {}

    fun onTick(timerData: TimerData) {}

    fun onFinish() {}

    fun onDelete() {}
}