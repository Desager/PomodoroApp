package com.example.pomodoroapp.interfaces

interface TimerAdapterListener {

    val id: Int

    fun onStart()

    fun onStop()

    fun onTick()

    fun onFinish()

    fun onDelete()
}