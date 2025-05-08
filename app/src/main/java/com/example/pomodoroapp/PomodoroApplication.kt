package com.example.pomodoroapp

import android.app.Application
import com.example.pomodoroapp.timer_manager.TimerManager

class PomodoroApplication : Application() {

    val timerManager = TimerManager()
}