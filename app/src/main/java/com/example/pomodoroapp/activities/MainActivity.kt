package com.example.pomodoroapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoroapp.PomodoroApplication
import com.example.pomodoroapp.R
import com.example.pomodoroapp.adapters.TimerAdapter
import com.example.pomodoroapp.databinding.ActivityMainBinding
import com.example.pomodoroapp.preferences.Preferences
import com.example.pomodoroapp.services.ForegroundService

class MainActivity : AppCompatActivity() {

    private val timerManager by lazy { (application as PomodoroApplication).timerManager }
    private val timerAdapter by lazy { TimerAdapter(timerManager) }
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val preferences by lazy { Preferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

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
    }

    override fun onStart() {
        super.onStart()

        val stopIntent = Intent(this@MainActivity, ForegroundService::class.java)
        stopService(stopIntent)

        timerManager.attachListener(timerAdapter)
        if (timerManager.isEmpty()) {
            restoreInstanceState()
        }
    }

    override fun onStop() {
        preferences.saveList(timerManager.getList())
        timerManager.detachListener(timerAdapter)
        if (timerManager.isStarted()) {
            val startIntent = Intent(this@MainActivity, ForegroundService::class.java)
            startIntent.putExtra(ForegroundService.COMMAND_ID, ForegroundService.COMMAND_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startIntent)
            } else {
                startService(startIntent)
            }
        }

        super.onStop()
    }

    private fun restoreInstanceState() {
        val timersList = preferences.restoreList()
        timerManager.setList(timersList)
        timerAdapter.submitList(timersList)
    }

    private fun requestNotificationPermission() {
        if (!isNotificationPermissionGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {

        private const val PERMISSION_REQUEST_CODE = 100
    }
}