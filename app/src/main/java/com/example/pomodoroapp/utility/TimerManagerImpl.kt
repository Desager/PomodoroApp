package com.example.pomodoroapp.utility

import android.os.CountDownTimer
import com.example.pomodoroapp.interfaces.TimerManagerListener
import com.example.pomodoroapp.items.TimerData

interface TimerManager {

    val listenersId: Int?

    fun attachListener(timerManagerListener: TimerManagerListener)

    fun detachListener(timerManagerListener: TimerManagerListener)

    fun getList(): List<TimerData>

    fun setList(timerList: List<TimerData>)

    fun add(seconds: Long)

    fun delete(id: Int)

    fun start(id: Int, millisInFuture: Long)

    fun stop()
}

object TimerManagerImpl : TimerManager {

    const val TICK_INTERVAL_S = 1L

    private var nextId = 0
    private val currentList: MutableList<TimerData> = mutableListOf()

    private var timer: CountDownTimer? = null
    private val listeners: MutableList<TimerManagerListener> = mutableListOf()
    private var timerData: TimerData? = null
    override val listenersId: Int?
        get() = timerData?.id

    override fun attachListener(timerManagerListener: TimerManagerListener) {
        if (timerManagerListener in listeners) {
            return
        }
        listeners.add(timerManagerListener)
        if (timer != null && timerData != null) {
            timerManagerListener.onStart(timerData!!.copy())
        }
    }

    override fun detachListener(timerManagerListener: TimerManagerListener) {
        listeners.remove(timerManagerListener)
    }

    override fun getList(): List<TimerData> {
        return currentList.map { it.copy() }
    }

    override fun setList(timerList: List<TimerData>) {
        detachAll()
        timer?.cancel()
        timer = null

        currentList.clear()
        currentList.addAll(timerList.map { it.copy() })
    }

    override fun add(seconds: Long) {
        currentList.add(TimerData(nextId++, seconds))
    }

    override fun delete(id: Int) {
        if (listenersId == id && timerData != null) {
            timer?.cancel()
            timer = null

            notifyAll { onStop(timerData!!.copy()) }
        }
        notifyAll { onDelete() }
        detachAll()
        currentList.removeIf { it.id == id }
    }

    override fun start(id: Int, millisInFuture: Long) {
        if (listenersId != id) {
            stop()

            timer = getCountDownTimer(millisInFuture)
            timer?.start()

            timerData = getTimerData(id)
            timerData!!.isStarted = true
        }
    }

    override fun stop() {
        if (timerData == null) {
            return
        }

        timer?.cancel()
        timer = null

        timerData!!.isStarted = false

        notifyAll { onStop(timerData!!.copy()) }
        detachAll()
    }

    private fun notifyAll(action: TimerManagerListener.() -> Unit) {
        listeners.forEach(action)
    }

    private fun detachAll() {
        listeners.clear()
        timerData = null
    }

    private fun getTimerData(id: Int): TimerData {
        return requireNotNull(currentList.find { it.id == id })
    }

    private fun getCountDownTimer(millisInFuture: Long): CountDownTimer {
        return object : CountDownTimer(millisInFuture, TICK_INTERVAL_S * 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timerData?.apply {
                    currentS -= TICK_INTERVAL_S
                    notifyAll { onTick(copy()) }
                }

            }

            override fun onFinish() {
                timerData?.apply {
                    currentS = totalS
                    isStarted = false

                    notifyAll { onFinish(copy()) }
                }

                detachAll()
                timer = null
            }
        }
    }
}