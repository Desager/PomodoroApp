package com.example.pomodoroapp.interfaces

import com.example.pomodoroapp.items.TimerData

interface TimerManagerListener {

    fun onStart(timerData: TimerData) {}

    fun onStop(timerData: TimerData) {}

    fun onTick(timerData: TimerData) {}

    fun onFinish(timerData: TimerData) {}

    fun onDelete() {}
}