package com.orsteg.gesos.androidquizkit

import android.os.Bundle

interface HistoryComponent {

    fun getQuiz(): Quiz

    fun saveToHistory(isTemporal: Boolean = false)

    fun saveToBundle(outState: Bundle?)

    fun restoreState(inState: Bundle?, timeStamp: Long? = null, isTemporal: Boolean = false)

}