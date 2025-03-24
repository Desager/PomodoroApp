package com.example.pomodoroapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoroapp.adapters.TimerAdapter
import com.example.pomodoroapp.databinding.ActivityMainBinding
import com.example.pomodoroapp.interfaces.MainActivityListener

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
                val mainActivityListener = timerAdapter as MainActivityListener
                mainActivityListener.add(seconds)
            }
        }
    }
}