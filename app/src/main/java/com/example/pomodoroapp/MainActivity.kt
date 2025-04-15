package com.example.pomodoroapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoroapp.adapters.TimerAdapter
import com.example.pomodoroapp.databinding.ActivityMainBinding
import com.example.pomodoroapp.items.TimerData
import com.example.pomodoroapp.services.ForegroundService
import com.example.pomodoroapp.utility.TimerManagerImpl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val timerAdapter = TimerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            setContentView(root)

            recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = timerAdapter
            }

            addTimerButton.setOnClickListener {
                if (editText.text.isEmpty()) {
                    Toast.makeText(this@MainActivity, resources.getString(R.string.warning_empty), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val seconds = editText.text.toString().toLong()
                if (seconds <= 0) {
                    Toast.makeText(this@MainActivity, resources.getString(R.string.warning_zero), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                TimerManagerImpl.add(seconds)
                timerAdapter.submitList(TimerManagerImpl.getList())
            }
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                val listenersId = TimerManagerImpl.listenersId
                if (listenersId != null) {
                    val startIntent = Intent(this@MainActivity, ForegroundService::class.java)
                    startIntent.putExtra(ForegroundService.COMMAND_ID, ForegroundService.COMMAND_START)
                    startIntent.putExtra(ForegroundService.TIMER_DATA_ID, listenersId)
                    startService(startIntent)
                }

                Log.d(TAG, "Saving instance state")
                val timersList = TimerManagerImpl.getList()
                val jsonTimersList = Gson().toJson(timersList)
                val preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                preferences.edit {
                    putString(TIMERS_LIST_KEY, jsonTimersList)
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                val stopIntent = Intent(this@MainActivity, ForegroundService::class.java)
                stopIntent.putExtra(ForegroundService.COMMAND_ID, ForegroundService.COMMAND_STOP)
                startService(stopIntent)
            }
        })

        val preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val jsonTimersList = preferences.getString(TIMERS_LIST_KEY, null) ?: return
        Log.d(TAG, "Restoring instance state")
        val type = object : TypeToken<List<TimerData>>() {}.type
        val timersList = Gson().fromJson<List<TimerData>>(jsonTimersList, type)

        TimerManagerImpl.setList(timersList)
        timerAdapter.submitList(TimerManagerImpl.getList())
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val TIMERS_LIST_KEY = "TIMERS_LIST_KEY"
        private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"
    }
}