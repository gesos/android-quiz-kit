package com.orsteg.gesos.androidquizkit.components

import android.os.Bundle
import com.orsteg.gesos.androidquizkit.quiz.Quiz

interface HistoryComponent {

    fun getQuiz(): Quiz

    fun saveToHistory(isTemporal: Boolean = false)

    fun saveToBundle(outState: Bundle?)

    fun restoreState(inState: Bundle?, timeStamp: Long? = null, isTemporal: Boolean = false)

}