package com.example.pomodoroapp.adapters

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoroapp.R
import com.example.pomodoroapp.databinding.TimerViewholderBinding
import com.example.pomodoroapp.interfaces.TimerManagerListener
import com.example.pomodoroapp.items.TimerData
import com.example.pomodoroapp.utility.TimerManagerImpl

class TimerAdapter: ListAdapter<TimerData, TimerAdapter.TimerViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TimerViewholderBinding.inflate(layoutInflater, parent, false)
        return TimerViewHolder(binding, parent.resources)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TimerViewHolder(
        private val binding: TimerViewholderBinding,
        private val resources: Resources
    ): RecyclerView.ViewHolder(binding.root), TimerManagerListener {

        fun bind(timerData: TimerData) {
            binding.timer.text = timerData.currentS.displayTime()
            binding.progressBar.setProgress(timerData.currentS.toFloat() / timerData.totalS)

            if (timerData.isStarted) {
                TimerManagerImpl.start(timerData.id, timerData.currentS * 1000)
                TimerManagerImpl.attachListener(this)
            }

            binding.startPauseButton.setOnClickListener {
                if (timerData.isStarted) {
                    TimerManagerImpl.stop()
                } else {
                    TimerManagerImpl.start(timerData.id, timerData.currentS * 1000)
                    TimerManagerImpl.attachListener(this)
                }
            }

            binding.deleteButton.setOnClickListener {
                TimerManagerImpl.delete(timerData.id)
                submitList(TimerManagerImpl.getList())
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

        override fun onStart(timerData: TimerData) {
            setIsRecyclable(false)

            binding.startPauseButton.text = resources.getString(R.string.stop)
            binding.blinkingIndicator.isInvisible = false
            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        }

        override fun onStop(timerData: TimerData) {
            binding.startPauseButton.text = resources.getString(R.string.start)
            binding.blinkingIndicator.isInvisible = true
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }

        override fun onTick(timerData: TimerData) {
            binding.timer.text = timerData.currentS.displayTime()
            binding.progressBar.setProgress(timerData.currentS.toFloat() / timerData.totalS)
        }

        override fun onFinish(timerData: TimerData) {
            onStop(timerData)
            setIsRecyclable(true)

            binding.timer.text = timerData.currentS.displayTime()
            binding.progressBar.resetProgress()
        }

        override fun onDelete() {
            setIsRecyclable(true)
        }
    }

    private companion object {

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