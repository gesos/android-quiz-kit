package com.orsteg.androidquizkit

import android.os.CountDownTimer

abstract class QuizTimer(val totalTimeInMillis: Long, val tickInterval: Long = INTERVAL_SECONDS) {

    private var mTimer: CountDownTimer? = null
    var isPlaying: Boolean = false
    var tickTime: Long = 0

    private fun getTimer(seekTime: Long): CountDownTimer = object : CountDownTimer(seekTime, tickInterval) {
        override fun onFinish() {
            isPlaying = false

            onTimerFinish()
        }
        override fun onTick(millisUntilFinished: Long) {
            tickTime = totalTimeInMillis - millisUntilFinished

            onTimerTick(tickTime)
        }
    }

    fun pause() {
        if (isPlaying) {
            mTimer?.cancel()
            isPlaying = false
        }
    }

    fun start() {
        if (!isPlaying) {
            mTimer = getTimer(totalTimeInMillis - tickTime)
            mTimer?.start()
            isPlaying = true
        }
    }

    fun restart() {
        cancel()
        start()
    }

    fun cancel() {
        pause()
        tickTime = 0
    }

    abstract fun onTimerTick(passedTimeInMillis: Long)

    abstract fun onTimerFinish()

    companion object {
        val INTERVAL_SECONDS = 1000L
        val INTERVAL_MINUTES = 60000L
    }
}
