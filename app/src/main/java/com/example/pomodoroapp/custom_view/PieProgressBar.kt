package com.example.pomodoroapp.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class PieProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultAttrs: Int = 0
) : View(context, attrs, defaultAttrs) {

    private val painter = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private var progressPercent = START_PROGRESS

    override fun onDraw(canvas: Canvas) {
        canvas.apply {
            val angle = calculateAngle(progressPercent)
            drawArc(0F, 0F, width.toFloat(), height.toFloat(), START_ANGLE, angle, true, painter)
        }
    }

    private fun calculateAngle(progressPercent: Float): Float {
        return 360 - 360 * progressPercent
    }

    fun setProgress(progressPercent: Float) {
        Log.d(DEBUG_TAG, "Timer progress: $progressPercent")

        this.progressPercent = progressPercent
        invalidate()
    }

    fun resetProgress() {
        setProgress(START_PROGRESS)
    }

    private companion object {
        private const val START_ANGLE = -90F
        private const val START_PROGRESS = 1F

        private const val DEBUG_TAG = "TIMER"
    }
}