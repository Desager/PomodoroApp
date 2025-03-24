package com.example.pomodoroapp.interfaces

interface TimerManager {

    fun delete(timerAdapterListener: TimerAdapterListener)

    fun start(timerAdapterListener: TimerAdapterListener)

    fun stop()

    fun finish()
}