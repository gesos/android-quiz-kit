package com.orsteg.gesos.androidquizkit

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer

class TimedQuiz(val mQuiz: Quiz, val totalTimeInMillis: Long, val tickInterval: Long = INTERVAL_SECONDS)
        : QuizHistory.HistoryInterface,  QuizController by mQuiz {

    private var mTimer: CountDownTimer? = null
    var isPlaying: Boolean = false
    var tickTime: Long = 0


    override fun getQuiz(): Quiz = mQuiz

    override fun saveToHistory(isTemporal: Boolean) {
        val head = if (isTemporal) "temp_" else ""

    }

    override fun saveToBundle(outState: Bundle?) {
        outState?.apply {

        }
    }

    override fun restoreState(inState: Bundle?, timeStamp: Long?, isTemporal: Boolean) {
        inState?.apply {

        }?:timeStamp?.apply {
            val head = if (isTemporal) "temp_" else ""

        }
    }

    private fun getTimer(seekTime: Long): CountDownTimer = object : CountDownTimer(seekTime, tickInterval) {
        override fun onFinish() {
            isPlaying = false

        }
        override fun onTick(millisUntilFinished: Long) {
            tickTime = totalTimeInMillis - millisUntilFinished

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

    class TimedStats: QuizHistory.Stats() {
        var totalTime: Long? = null
        var finishTime: Long? = null

    }


    interface OnTimeChangeListener {
        fun onTimerTick(passedTimeInMillis: Long)

        fun onTimerFinish()
    }

    companion object {

        fun getStat(context: Context, id: Long): TimedStats? {
             return QuizHistory.getInstance(context).getStat(id, TimedStats())?.apply {

                 // Implement your stat recovery here
             }
        }

        fun deleteHistory(context: Context, it: Long, isTemporal: Boolean = false) {
            QuizHistory.getInstance(context).deleteHistory(it, isTemporal)

            // Implement your own house cleaning
        }

        val INTERVAL_SECONDS = 1000L
        val INTERVAL_MINUTES = 60000L
    }
}
