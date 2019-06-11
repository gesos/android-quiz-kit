package com.orsteg.gesos.androidquizkit

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import java.util.*

class TimedQuiz(val mQuiz: Quiz, var totalTimeInMillis: Long, val tickInterval: Long = INTERVAL_SECONDS)
        : HistoryComponent,  QuizController by mQuiz {

    private var mTimer: CountDownTimer? = null
    var isPlaying: Boolean = false
    var tickTime: Long = 0
    var lastPauseTime: Long = -1
    var hasFinished: Boolean = false
    var onTimeChangeListener: OnTimeChangeListener? = null


    private fun getTimer(seekTime: Long): CountDownTimer = object : CountDownTimer(seekTime, tickInterval) {
        override fun onFinish() {
            isPlaying = false
            hasFinished = true

            tickTime = totalTimeInMillis

            (mQuiz.getContext() as Activity).runOnUiThread {
                onTimeChangeListener?.onTimerFinish()
            }
        }
        override fun onTick(millisUntilFinished: Long) {
            tickTime = totalTimeInMillis - millisUntilFinished

            (mQuiz.getContext() as Activity).runOnUiThread {
                onTimeChangeListener?.onTimerTick(millisUntilFinished)
            }
        }
    }

    private fun getPreferences() = Companion.getPreferences(mQuiz.getContext())

    private fun editPreferences() = getPreferences().edit()

    override fun getQuiz(): Quiz = mQuiz

    override fun saveToHistory(isTemporal: Boolean) {

        editPreferences().apply {
            if (isTemporal) putLong("${mQuiz.id}_quiz_pause_time", lastPauseTime)
            putLong("${mQuiz.id}_quiz_tick_time", tickTime)
            putLong("${mQuiz.id}_quiz_total_time", totalTimeInMillis)
        }.commit()
    }

    override fun saveToBundle(outState: Bundle?) {
        outState?.apply {
            putLong("quiz_tick_time", tickTime)
            putLong("quiz_pause_time", lastPauseTime)
            putLong("quiz_total_time", totalTimeInMillis)
        }
    }

    override fun restoreState(inState: Bundle?, timeStamp: Long?, isTemporal: Boolean) {
        inState?.apply {
            tickTime = getLong("quiz_tick_time")
            lastPauseTime = getLong("quiz_pause_time")
            totalTimeInMillis = getLong("quiz_total_time")
        }?:timeStamp?.also {
            getPreferences().apply {
                if (isTemporal) {
                    lastPauseTime = getLong ("${it}_quiz_pause_time", -1)
                } else {
                    hasFinished = true
                    lastPauseTime = -1
                }
                tickTime = getLong("${it}_quiz_tick_time", 0L)
                totalTimeInMillis = getLong("${it}_quiz_total_time", 0)
            }
        }
    }

    fun start() {
        if (!isPlaying && !hasFinished) {
            Log.d("timer", "on start $hasFinished")
            // calculate new tick time
            if (lastPauseTime != -1L) {
                tickTime += Calendar.getInstance().timeInMillis - lastPauseTime
            }

            if (tickTime < totalTimeInMillis) {
                mTimer = getTimer(totalTimeInMillis - tickTime)
                mTimer?.start()
                isPlaying = true
            } else {
                hasFinished = true
                tickTime = totalTimeInMillis
                onTimeChangeListener?.apply {
                    onTimerTick(0)
                    onTimerFinish()
                }
            }
        }
    }

    fun pause() {
        if (isPlaying) {
            mTimer?.cancel()
            isPlaying = false
            lastPauseTime = Calendar.getInstance().timeInMillis
        }
        Log.d("timer", "on pause $hasFinished")
    }

    fun suspend() {
        if (isPlaying) {
            mTimer?.cancel()
            isPlaying = false
            lastPauseTime = -1
        }
    }

    fun cancel() {
        pause()
        lastPauseTime = -1
        tickTime = 0
        hasFinished = false
    }

    fun finish() {
        pause()
        lastPauseTime = -1
        hasFinished = true
    }

    fun restart() {
        cancel()
        start()
    }

    class TimedStats: QuizHistory.Stats() {
        var totalTime: Long? = null
        var finishTime: Long? = null

    }


    interface OnTimeChangeListener {
        fun onTimerTick(timeLeft: Long)

        fun onTimerFinish()
    }

    companion object {

        private fun getPreferences(context: Context) = context.getSharedPreferences("timed_quiz_history", Activity.MODE_PRIVATE)

        private fun editPreferences(context: Context) = getPreferences(context).edit()

        fun getStat(context: Context, id: Long): TimedStats? {
             return QuizHistory.getInstance(context).getStat(id, TimedStats())?.apply {
                 getPreferences(context).also {
                     totalTime = it.getLong("${id}_quiz_total_time", 0)
                     finishTime = it.getLong("${id}_quiz_tick_time", 0)
                 }
             }
        }

        fun deleteHistory(context: Context, it: Long, isTemporal: Boolean = false) {
            QuizHistory.getInstance(context).deleteHistory(it, isTemporal)

            editPreferences(context).apply{
                if (isTemporal) remove("${it}_quiz_pause_time")
                remove("${it}_quiz_tick_time")
                remove("${it}_quiz_total_time")
            }.commit()

        }

        val INTERVAL_SECONDS = 1000L
        val INTERVAL_MINUTES = 60000L
    }
}
