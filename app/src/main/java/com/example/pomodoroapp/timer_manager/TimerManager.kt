package com.example.pomodoroapp.timer_manager

import android.os.CountDownTimer

class TimerManager private constructor() {

    private var nextId = 0
    private val currentList: MutableList<TimerData> = mutableListOf()

    private var timer: Timer? = null
    private val listeners: MutableList<TimerManagerListener> = mutableListOf()

    fun isStarted() = timer != null

    fun attachListener(timerManagerListener: TimerManagerListener) {
        if (timerManagerListener in listeners) {
            return
        }
        listeners.add(timerManagerListener)
    }

    fun detachListener(timerManagerListener: TimerManagerListener) {
        listeners.remove(timerManagerListener)
    }

    fun getList(): List<TimerData> {
        return currentList.map { it.copy() }
    }

    fun setList(timerList: List<TimerData>) {
        timer?.stop()
        timer = null

        currentList.clear()
        currentList.addAll(timerList.map { it.copy() })
    }

    fun add(seconds: Long) {
        currentList.add(TimerData(nextId++, seconds))
    }

    fun delete(id: Int) {
        if (timer?.id == id) {
            timer?.stop()
            timer = null
        }
        currentList.removeIf { it.id == id }
        notifyAll { onDelete() }
    }

    fun start(id: Int) {
        if (timer?.id == id) {
            return
        }
        timer?.stop()
        timer = Timer(getTimerData(id))
        timer?.start()
    }

    fun stop() {
        timer?.stop()
        timer = null
    }

    private fun notifyAll(action: TimerManagerListener.() -> Unit) {
        listeners.forEach(action)
    }

    private fun getTimerData(id: Int): TimerData {
        return requireNotNull(currentList.find { it.id == id })
    }

    inner class Timer(
        private val timerData: TimerData
    ) {

        val id = timerData.id
        private val countDownTimer: CountDownTimer = getCountDownTimer(timerData.currentS * 1000)

        fun start() {
            timerData.isStarted = true
            timerData.isFinished = false
            countDownTimer.start()
        }

        fun stop() {
            countDownTimer.cancel()
            timerData.isStarted = false
            notifyAll { onStop() }
        }

        private fun getCountDownTimer(millisInFuture: Long): CountDownTimer {
            return object : CountDownTimer(millisInFuture, TICK_INTERVAL_S * 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    timerData.currentS -= TICK_INTERVAL_S
                    notifyAll { onTick(timerData.copy()) }
                }

                override fun onFinish() {
                    timerData.currentS = timerData.totalS
                    timerData.isStarted = false
                    timerData.isFinished = true
                    notifyAll { onFinish() }
                    timer = null
                }
            }
        }
    }

    companion object {

        private const val TICK_INTERVAL_S = 1L
        private val instance = TimerManager()

        fun getInstance() = instance
    }
}