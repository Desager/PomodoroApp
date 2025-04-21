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
import com.example.pomodoroapp.timer_manager.TimerData
import com.example.pomodoroapp.timer_manager.TimerManager
import com.example.pomodoroapp.timer_manager.TimerManagerListener
import com.example.pomodoroapp.utils.displayTime

class TimerAdapter: ListAdapter<TimerData, TimerAdapter.TimerViewHolder>(itemComparator),
    TimerManagerListener {

    private val timerManager = TimerManager.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TimerViewholderBinding.inflate(layoutInflater, parent, false)
        return TimerViewHolder(binding, parent.resources)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onStart() = submitList(timerManager.getList())

    override fun onStop() = submitList(timerManager.getList())

    override fun onTick(timerData: TimerData) = submitList(timerManager.getList())

    override fun onFinish() = submitList(timerManager.getList())

    override fun onDelete() = submitList(timerManager.getList())

    inner class TimerViewHolder(
        private val binding: TimerViewholderBinding,
        private val resources: Resources
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(timerData: TimerData) {
            binding.timer.text = timerData.currentS.displayTime(resources)
            binding.progressBar.setProgress(timerData.currentS.toFloat() / timerData.totalS)

            if (timerData.isStarted) {
                binding.startPauseButton.text = resources.getString(R.string.stop)
                binding.blinkingIndicator.isInvisible = false
                val blinkingIndicatorBackground = (binding.blinkingIndicator.background as? AnimationDrawable)
                if (blinkingIndicatorBackground?.isRunning == false) {
                    blinkingIndicatorBackground.start()
                }
            } else {
                binding.startPauseButton.text = resources.getString(R.string.start)
                binding.blinkingIndicator.isInvisible = true
                (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
            }

            if (timerData.isFinished) {
                binding.timer.text = timerData.currentS.displayTime(resources)
                binding.progressBar.resetProgress()
                binding.root.setBackgroundColor(resources.getColor(R.color.red_200, binding.root.context.theme))
            } else {
                binding.root.setBackgroundColor(resources.getColor(R.color.white, binding.root.context.theme))
            }

            binding.startPauseButton.setOnClickListener {
                if (timerData.isStarted) {
                    timerManager.stop()
                } else {
                    timerManager.start(timerData.id)
                }
            }

            binding.deleteButton.setOnClickListener {
                timerManager.delete(timerData.id)
            }
        }
    }

    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<TimerData>() {

            override fun areItemsTheSame(oldItem: TimerData, newItem: TimerData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TimerData, newItem: TimerData): Boolean {
                return oldItem.totalS == newItem.totalS &&
                        oldItem.currentS == newItem.currentS &&
                        oldItem.isStarted == newItem.isStarted &&
                        oldItem.isFinished == newItem.isFinished
            }

            override fun getChangePayload(oldItem: TimerData, newItem: TimerData) = Any()
        }
    }
}