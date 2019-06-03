package com.orsteg.gesos.androidquizkit

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer

class TimedQuiz(val mQuiz: Quiz, val totalTimeInMillis: Long, val tickInterval: Long = INTERVAL_SECONDS)
        : QuizHistory.HistoryInterface,  QuizController by mQuiz {

    private var mTimer: CountDownTimer? = null
    var isPlaying: Boolean = false
    var tickTime: Long = 0
    var onTimeChangeListener: OnTimeChangeListener? = null


    override fun getQuiz(): Quiz = mQuiz

    override fun saveToHistory(isTemporal: Boolean) {

        if (isTemporal) {
            // save state
            editPreferences().putLong("temp_quiz_tick_time", tickTime)
        } else {
            // save stats
            editPreferences().putLong("quiz_finish_time", tickTime)
        }
    }

    override fun saveToBundle(outState: Bundle?) {
        outState?.apply {
            putLong("quiz_tick_time", tickTime)
        }
    }

    override fun restoreState(inState: Bundle?, timeStamp: Long?, isTemporal: Boolean) {
        inState?.apply {
            tickTime = getLong("quiz_tick_time")
        }?:timeStamp?.apply {
            if (isTemporal) {
                // restore state
                tickTime = getPreferences().getLong("temp_quiz_tick_time", 0L)
            } else {

            }
        }
    }

    private fun getTimer(seekTime: Long): CountDownTimer = object : CountDownTimer(seekTime, tickInterval) {
        override fun onFinish() {
            isPlaying = false

            (mQuiz.getContext() as Activity).runOnUiThread {
                onTimeChangeListener?.onTimerFinish()
            }
        }
        override fun onTick(millisUntilFinished: Long) {
            tickTime = totalTimeInMillis - millisUntilFinished

            (mQuiz.getContext() as Activity).runOnUiThread {
                onTimeChangeListener?.onTimerTick(tickTime)
            }
        }
    }

    private fun getPreferences() = mQuiz.getContext().getSharedPreferences("quiz_time_history", Activity.MODE_PRIVATE)

    private fun editPreferences() = getPreferences().edit()

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
