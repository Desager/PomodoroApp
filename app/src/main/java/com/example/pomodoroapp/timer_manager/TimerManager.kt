package com.example.pomodoroapp.timer_manager

import android.os.CountDownTimer

class TimerManager {

    private var nextId = 0
    private val currentList: MutableList<TimerData> = mutableListOf()

    private var timer: Timer? = null
    private val listeners: MutableList<TimerManagerListener> = mutableListOf()

    fun isStarted() = timer != null

    fun isEmpty() = currentList.isEmpty()

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
        return currentList.toList()
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
        val timerDataIndex = getTimerDataIndex(id)
        val timerData = currentList[timerDataIndex]
        timer = Timer(timerDataIndex, timerData)
        timer?.start()
    }

    fun stop() {
        timer?.stop()
        timer = null
    }

    private fun notifyAll(action: TimerManagerListener.() -> Unit) {
        listeners.forEach(action)
    }

    private fun getTimerDataIndex(id: Int): Int {
        return requireNotNull(currentList.indexOfFirst { it.id == id })
    }

    private fun changeTimerData(index: Int, newTimerData: TimerData): TimerData {
        currentList[index] = newTimerData
        return newTimerData
    }

    inner class Timer(
        private val timerDataIndex: Int,
        private var timerData: TimerData
    ) {
        val id = timerData.id
        private val countDownTimer: CountDownTimer = getCountDownTimer(timerData.currentS * 1000)

        fun start() {
            timerData = changeTimerData(timerDataIndex, timerData.copy(
                isStarted = true,
                isFinished = false
            ))
            countDownTimer.start()
        }

        fun stop() {
            countDownTimer.cancel()
            timerData = changeTimerData(timerDataIndex, timerData.copy(
                isStarted = false
            ))
            notifyAll { onStop() }
        }

        private fun getCountDownTimer(millisInFuture: Long): CountDownTimer {
            return object : CountDownTimer(millisInFuture, TICK_INTERVAL_S * 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    timerData = changeTimerData(timerDataIndex, timerData.copy(
                        currentS = timerData.currentS - TICK_INTERVAL_S
                    ))
                    notifyAll { onTick(timerData) }
                }

                override fun onFinish() {
                    timerData = changeTimerData(timerDataIndex, timerData.copy(
                        currentS = timerData.totalS,
                        isStarted = false,
                        isFinished = true
                    ))
                    notifyAll { onFinish() }
                    timer = null
                }
            }
        }
    }

    companion object {

        private const val TICK_INTERVAL_S = 1L
    }
}