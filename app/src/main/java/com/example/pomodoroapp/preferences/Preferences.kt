package com.example.pomodoroapp.preferences

import android.content.Context
import androidx.core.content.edit
import com.example.pomodoroapp.timer_manager.TimerData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Preferences(
    context: Context
) {

    private val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun saveList(list: List<TimerData>) {
        val jsonTimersList = Gson().toJson(list)
        sharedPreferences.edit {
            putString(TIMERS_LIST_KEY, jsonTimersList)
        }
    }

    fun restoreList(): List<TimerData> {
        val jsonTimersList = sharedPreferences.getString(TIMERS_LIST_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<TimerData>>() {}.type
        val timersList = Gson().fromJson<List<TimerData>>(jsonTimersList, type)
        return timersList
    }

    private companion object {

        private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"
        private const val TIMERS_LIST_KEY = "TIMERS_LIST_KEY"
    }
}