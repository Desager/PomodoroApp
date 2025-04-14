package com.example.pomodoroapp.adapters

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoroapp.R
import com.example.pomodoroapp.databinding.TimerViewholderBinding
import com.example.pomodoroapp.interfaces.MainActivityListener
import com.example.pomodoroapp.interfaces.TimerAdapterListener
import com.example.pomodoroapp.interfaces.TimerManager
import com.example.pomodoroapp.items.TimerData

class TimerAdapter: ListAdapter<TimerData, TimerAdapter.TimerViewHolder>(itemComparator),
    MainActivityListener, TimerManager {

    private var nextId = 0
    private var timer: CountDownTimer? = null
    private var workingTimer: TimerAdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TimerViewholderBinding.inflate(layoutInflater, parent, false)
        return TimerViewHolder(binding, parent.resources)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun add(seconds: Long) {
        val newList = currentList.toMutableList().apply { add(TimerData(nextId++, seconds)) }
        submitList(newList)
    }

    override fun delete(timerAdapterListener: TimerAdapterListener) {
        if (workingTimer?.id == timerAdapterListener.id) {
            stop()
        }
        timerAdapterListener.onDelete()
        val newList = currentList.toMutableList().apply { remove(find { it.id == timerAdapterListener.id }) }
        submitList(newList)
    }

    override fun start(timerAdapterListener: TimerAdapterListener, millisInFuture: Long) {
        if (workingTimer?.id != timerAdapterListener.id) {
            workingTimer?.onStop()

            timer?.cancel()
            timer = getCountDownTimer(millisInFuture)
            timer?.start()
        }
        workingTimer = timerAdapterListener
        workingTimer?.onStart()
    }

    override fun stop() {
        workingTimer?.onStop()
        workingTimer = null

        timer?.cancel()
        timer = null
    }

    private fun getCountDownTimer(millisInFuture: Long): CountDownTimer {
        return object : CountDownTimer(millisInFuture, TICK_INTERVAL_S * 1000) {

            override fun onTick(millisUntilFinished: Long) {
                workingTimer?.onTick()
            }

            override fun onFinish() {
                workingTimer?.onFinish()
                workingTimer = null
                timer = null
            }
        }
    }

    inner class TimerViewHolder(
        private val binding: TimerViewholderBinding,
        private val resources: Resources
    ): RecyclerView.ViewHolder(binding.root), TimerAdapterListener {

        private lateinit var timerData: TimerData
        override val id: Int get() = timerData.id

        fun bind(timerData: TimerData) {
            this.timerData = timerData
            binding.timer.text = timerData.currentS.displayTime()
            binding.progressBar.setProgress(timerData.currentS.toFloat() / timerData.totalS)

            if (timerData.isStarted) {
                start(this, timerData.currentS * 1000)
            }

            binding.startPauseButton.setOnClickListener {
                if (timerData.isStarted) {
                    stop()
                } else {
                    start(this, timerData.currentS * 1000)
                }
            }

            binding.deleteButton.setOnClickListener {
                delete(this)
            }
        }

        private fun Long.displayTime(): String {
            if (this <= 0L) {
                return resources.getString(R.string.time, 0, 0, 0)
            }
            val h = this / 3600
            val m = this % 3600 / 60
            val s = this % 60

            return resources.getString(R.string.time, h, m, s)
        }

        override fun onStart() {
            timerData.isStarted = true
            setIsRecyclable(false)

            binding.startPauseButton.text = resources.getString(R.string.stop)
            binding.blinkingIndicator.isInvisible = false
            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        }

        override fun onStop() {
            timerData.isStarted = false

            binding.startPauseButton.text = resources.getString(R.string.start)
            binding.blinkingIndicator.isInvisible = true
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }

        override fun onTick() {
            timerData.currentS -= TICK_INTERVAL_S

            binding.timer.text = timerData.currentS.displayTime()
            binding.progressBar.setProgress(timerData.currentS.toFloat() / timerData.totalS)
        }

        override fun onFinish() {
            onStop()
            setIsRecyclable(true)

            timerData.currentS = timerData.totalS
            binding.timer.text = timerData.currentS.displayTime()
            binding.progressBar.resetProgress()
        }

        override fun onDelete() {
            setIsRecyclable(true)
        }
    }

    private companion object {

        private const val TICK_INTERVAL_S = 1L

        private val itemComparator = object : DiffUtil.ItemCallback<TimerData>() {

            override fun areItemsTheSame(oldItem: TimerData, newItem: TimerData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TimerData, newItem: TimerData): Boolean {
                return oldItem.currentS == newItem.currentS &&
                        oldItem.isStarted == newItem.isStarted
            }
        }
    }
}