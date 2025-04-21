package com.example.pomodoroapp.utils

import android.content.res.Resources
import com.example.pomodoroapp.R

fun Long.displayTime(resources: Resources): String {
    if (this <= 0L) {
        return resources.getString(R.string.time, 0, 0, 0)
    }
    val h = this / 3600
    val m = this % 3600 / 60
    val s = this % 60

    return resources.getString(R.string.time, h, m, s)
}