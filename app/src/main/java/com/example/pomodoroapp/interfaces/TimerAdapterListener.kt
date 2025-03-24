package com.example.pomodoroapp.interfaces

import android.os.CountDownTimer

interface TimerAdapterListener {

    val id: Int

    fun start()

    fun stop()

    fun getCountDownTimer(): CountDownTimer
}