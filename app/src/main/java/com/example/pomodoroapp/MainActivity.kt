package com.example.pomodoroapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoroapp.adapters.TimerAdapter
import com.example.pomodoroapp.databinding.ActivityMainBinding
import com.example.pomodoroapp.services.ForegroundService
import com.example.pomodoroapp.timer_manager.TimerData
import com.example.pomodoroapp.timer_manager.TimerManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val timerManager = TimerManager.getInstance()
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val timerAdapter = TimerAdapter()
    private val preferences by lazy { getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isPermissionGranted()) {
            requestPermission()
        }

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
                timerManager.add(seconds)
                timerAdapter.submitList(timerManager.getList())
            }
        }

        timerManager.attachListener(timerAdapter)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                if (timerManager.isStarted()) {
                    val startIntent = Intent(this@MainActivity, ForegroundService::class.java)
                    startIntent.putExtra(ForegroundService.COMMAND_ID, ForegroundService.COMMAND_START)
                    startService(startIntent)
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                val stopIntent = Intent(this@MainActivity, ForegroundService::class.java)
                stopIntent.putExtra(ForegroundService.COMMAND_ID, ForegroundService.COMMAND_STOP)
                startService(stopIntent)
            }
        })

        restoreInstanceState()
    }

    override fun onDestroy() {
        timerManager.detachListener(timerAdapter)
        saveInstanceState()
        super.onDestroy()
    }

    private fun saveInstanceState() {
        val timersList = timerManager.getList()
        val jsonTimersList = Gson().toJson(timersList)
        preferences.edit {
            putString(TIMERS_LIST_KEY, jsonTimersList)
        }
    }

    private fun restoreInstanceState() {
        val jsonTimersList = preferences.getString(TIMERS_LIST_KEY, null) ?: return
        val type = object : TypeToken<List<TimerData>>() {}.type
        val timersList = Gson().fromJson<List<TimerData>>(jsonTimersList, type)
        timerManager.setList(timersList)
        timerAdapter.submitList(timerManager.getList())
    }

    @SuppressLint("InlinedApi")
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("InlinedApi")
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
    }

    private companion object {

        private const val TIMERS_LIST_KEY = "TIMERS_LIST_KEY"
        private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"

        private const val PERMISSION_REQUEST_CODE = 100
    }
}