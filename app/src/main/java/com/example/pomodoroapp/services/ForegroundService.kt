package com.example.pomodoroapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.pomodoroapp.MainActivity
import com.example.pomodoroapp.R
import com.example.pomodoroapp.interfaces.TimerManagerListener
import com.example.pomodoroapp.items.TimerData
import com.example.pomodoroapp.utility.TimerManagerImpl

class ForegroundService : Service(), TimerManagerListener {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null

    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(CONTENT_TITLE)
            .setGroup(GROUP)
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
    }

    override fun onTick(timerData: TimerData) {
        notificationManager?.notify(
            NOTIFICATION_ID,
            getNotification(timerData.currentS.displayTime())
        )
    }

    override fun onFinish(timerData: TimerData) {
        commandStop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: COMMAND_INVALID) {
            COMMAND_START -> {
                val timerDataId = intent?.extras?.getInt(TIMER_DATA_ID)
                val timerData = TimerManagerImpl.getList().find { it.id == timerDataId } ?: return
                commandStart(timerData)
            }
            COMMAND_STOP -> commandStop()
            COMMAND_INVALID -> return
        }
    }

    private fun commandStart(timerData: TimerData) {
        if (isServiceStarted) {
            return
        }
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            TimerManagerImpl.start(timerData.id, timerData.currentS * 1000)
            TimerManagerImpl.attachListener(this)
        } finally {
            isServiceStarted = true
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        try {
            TimerManagerImpl.detachListener(this)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            notificationManager?.cancel(NOTIFICATION_ID)
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("")
        startForeground(NOTIFICATION_ID, notification)
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

    private fun getNotification(content: String) = builder.setContentTitle(content).build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, resultIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {

        const val TIMER_DATA_ID = "TIMER_DATA_ID"

        private const val CONTENT_TITLE = "Timer"
        private const val GROUP = "Timer"

        private const val CHANNEL_ID = "TimerChannel"
        private const val CHANNEL_NAME = "Pomodoro"
        private const val NOTIFICATION_ID = 101

        const val COMMAND_ID = "SERVICE_COMMAND"
        const val COMMAND_START = "COMMAND_START"
        const val COMMAND_STOP = "COMMAND_STOP"
        private const val COMMAND_INVALID = "INVALID"
    }
}