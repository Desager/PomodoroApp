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
    private val timersData: MutableList<TimerData> = mutableListOf()
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
        timersData.add(TimerData(nextId++, seconds))
        submitList(timersData.toList())
    }

    override fun delete(timerAdapterListener: TimerAdapterListener) {
        if (workingTimer?.id == timerAdapterListener.id) {
            stop()
        }
        timersData.remove(timersData.find { it.id == timerAdapterListener.id })
        submitList(timersData.toList())
    }

    override fun start(timerAdapterListener: TimerAdapterListener) {
        workingTimer?.stop()
        workingTimer = timerAdapterListener
        workingTimer?.start()

        timer?.cancel()
        timer = timerAdapterListener.getCountDownTimer()
        timer?.start()
    }

    override fun stop() {
        workingTimer?.stop()
        workingTimer = null

        timer?.cancel()
        timer = null
    }

    inner class TimerViewHolder(
        private val binding: TimerViewholderBinding,
        private val resources: Resources
    ): RecyclerView.ViewHolder(binding.root), TimerAdapterListener {

        lateinit var timerData: TimerData
        override val id: Int get() = timerData.id

        fun bind(timerData: TimerData) {
            this.timerData = timerData
            binding.timer.text = timerData.currentS.displayTime()
            binding.progressBar.setProgress(timerData.currentS.toFloat() / timerData.totalS)


            if (timerData.isStarted) {
                this@TimerAdapter.start(this)
            }

            binding.startPauseButton.setOnClickListener {
                if (timerData.isStarted) {
                    this@TimerAdapter.stop()
                } else {
                    this@TimerAdapter.start(this)
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

        override fun start() {
            timerData.isStarted = true
            setIsRecyclable(false)

            binding.startPauseButton.text = resources.getString(R.string.stop)
            binding.blinkingIndicator.isInvisible = false
            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        }

        override fun stop() {
            timerData.isStarted = false

            binding.startPauseButton.text = resources.getString(R.string.start)
            binding.blinkingIndicator.isInvisible = true
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }

        override fun getCountDownTimer(): CountDownTimer {
            return object : CountDownTimer(timerData.currentS * 1000, TICK_INTERVAL_S * 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    timerData.currentS -= TICK_INTERVAL_S
                    binding.timer.text = timerData.currentS.displayTime()
                    binding.progressBar.setProgress(timerData.currentS.toFloat() / timerData.totalS)
                }

                override fun onFinish() {
                    this@TimerAdapter.stop()
                    setIsRecyclable(true)
                    timerData.currentS = timerData.totalS
                    binding.timer.text = timerData.currentS.displayTime()
                    binding.progressBar.resetProgress()
                }
            }
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