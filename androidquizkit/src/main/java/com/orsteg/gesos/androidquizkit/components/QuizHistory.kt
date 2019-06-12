package com.orsteg.gesos.androidquizkit.components

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.orsteg.gesos.androidquizkit.quiz.Quiz


class QuizHistory private constructor(context: Context) {

    private val pref = context.getSharedPreferences("quiz_history", Activity.MODE_PRIVATE)
    private val editor = pref.edit()

    fun getAllStats(): List<Stats> = pref.getStringSet("quiz_ids")
            .map {
                pref.run {
                    Stats(
                        getString("${it}_topic", "") ?: "", it.toLong(), getStringSet("${it}_answers").toIntList(),
                        getStringSet("${it}_correct").toIntList(), getInt("${it}_count", 0)
                    )
                }
            }.sortedByDescending { stat -> stat.quizTimestamp }

    fun getStat(it: Long): Stats? {
        return getStat(it, Stats())
    }

    fun <S: Stats> getStat(it: Long, stat: S): S? {
        return if (pref.getStringSet("quiz_ids").contains(it.toString())) {
            pref.run {
                stat.apply {
                    initValues(
                        getString("${it}_topic", "")?:"", it, getStringSet("${it}_answers").toIntList(),
                        getStringSet("${it}_correct").toIntList(), getInt("${it}_count", 0)
                    )
                }
            }
        } else null
    }

    fun getHistoryList(isTemporal: Boolean = false): List<Int> {
        val head = if (isTemporal) "temp_" else ""

        return pref.getStringSet("${head}quiz_ids").toIntList()
    }

    fun deleteHistory(it: Long, isTemporal: Boolean = false): Boolean {
        val head = if (isTemporal) "temp_" else ""
        var success: Boolean

        // remove history id
        editor.putStringSet("${head}quiz_ids",
            pref.getStringSet("${head}quiz_ids").toMutableSet()
                .apply { success = remove(it.toString()) }
        )

        if (success) {
            editor.remove("$head${it}_index")
                .remove("$head${it}_init").remove("$head${it}_topic")
                .remove("$head${it}_maker").remove("$head${it}_select").apply()

            if (isTemporal) {
                editor.remove("$head${it}_pointer").apply()
            } else {
                // remove stats
                editor.remove("${it}_count").remove("${it}_answers").apply()
            }
        }
        editor.commit()

        return success
    }

    fun getHistory(it: Long, isTemporal: Boolean = false): History? {
        val head = if (isTemporal) "temp_" else ""

        return if (pref.getStringSet("${head}quiz_ids").contains(it.toString())) {
            pref.run {
                History(
                    getString("$head${it}_topic", "") ?: "", it, getStringSet("$head${it}_index").toIntList(),
                    getStringSet("$head${it}_init").toIntList(), getStringSet("$head${it}_select").toNullableIntList(),
                    getNullableInt("$head${it}_pointer")
                )
            }
        } else null
    }

    fun getMaker(it: Long, isTemporal: Boolean = false): String? {
        val head = if (isTemporal) "temp_" else ""

        return if (pref.getStringSet("${head}quiz_ids").contains(it.toString())) {
            pref.getString("$head${it}_maker", "")
        } else null
    }

    fun saveToHistory(hComponent: HistoryComponent, isTemporal: Boolean = false) {
        saveToHistory(hComponent.getQuiz(), isTemporal, hComponent::class.java.name)
        hComponent.saveToHistory(isTemporal)
    }

    fun saveToHistory(quiz: Quiz, isTemporal: Boolean = false, maker: String = this::class.java.name) {
        val head = if (isTemporal) "temp_" else ""

        // save history id
        editor.putStringSet("${head}quiz_ids",
            pref.getStringSet("${head}quiz_ids").toMutableSet()
                .plus("${quiz.id}")).apply()

        // save history data
        editor.putStringSet("$head${quiz.id}_index", quiz.questionIndexes.toStringSet())
            .putStringSet("$head${quiz.id}_init", quiz.initStates.toStringSet())
            .putStringSet("$head${quiz.id}_select", quiz.selectionState.toStringSet())
            .putString("$head${quiz.id}_topic", quiz.topic)
            .putString("$head${quiz.id}_maker", maker).apply()

        if (isTemporal) {
            editor.putInt("$head${quiz.id}_pointer", quiz.currentIndex).apply()
        } else {
            // generate stats
            val stats = Stats(quiz)

            editor.putInt("${quiz.id}_count", stats.questionCount)
                .putStringSet("${quiz.id}_answers", stats.answeredIndexes?.toStringSet())
                .putStringSet("${quiz.id}_correct", stats.correctIndexes?.toStringSet()).apply()
        }

        editor.commit()
    }

    private fun <T> List<T?>.toStringSet() = this.map { it?.toString()?:"" }.toSet()
    private fun Set<String>.toNullableIntList() = this.map { if (it != "") it.toInt() else null }.toList()
    private fun Set<String>.toIntList() = this.map { it.toInt() }.toList()
    private fun SharedPreferences.getStringSet(key: String): Set<String>
            = this.getStringSet(key, ArrayList<String>().toSet())?:ArrayList<String>().toSet()
    private fun SharedPreferences.getNullableLong(key: String): Long? {
        val l = this.getLong(key, -1L)
        return if (l != -1L) l else null
    }

    private fun SharedPreferences.getNullableInt(key: String): Int? {
        val l = this.getInt(key, -1)
        return if (l != -1) l else null
    }

    class History(val topic: String, val timeStamp: Long, val qIndexes: List<Int>, val initS: List<Int>, val selectS: List<Int?>, val pointer: Int?)

    open class Stats() {

        var topic: String = ""
        var quizTimestamp: Long = 0
        var answeredIndexes: List<Int>? = null
        var answeredCount: Int = 0
        var correctIndexes: List<Int>? = null
        var correctCount: Int = 0
        var questionCount: Int = 0

        constructor(quiz: Quiz) : this() {
            topic = quiz.topic
            quizTimestamp = quiz.id
            answeredIndexes =
                getAnsweredIndexes(quiz)
            answeredCount = answeredIndexes?.size ?: 0
            correctIndexes =
                getCorrectlyAnsweredIndexes(quiz)
            correctCount = correctIndexes?.size ?: 0
            questionCount = quiz.getTotalQuizQuestions()
        }

        constructor(
            topic: String, quizTimestamp: Long, answeredIndexes: List<Int>, correctIndexes: List<Int>,
            questionCount: Int
        ) : this() {
            initValues(topic, quizTimestamp, answeredIndexes, correctIndexes, questionCount)
        }

        fun initValues(
            topic: String, quizTimestamp: Long, answeredIndexes: List<Int>, correctIndexes: List<Int>,
            questionCount: Int) {

            this.topic = topic
            this.quizTimestamp = quizTimestamp
            this.answeredIndexes = answeredIndexes
            answeredCount = answeredIndexes.size
            this.correctIndexes = correctIndexes
            correctCount = correctIndexes.size
            this.questionCount = questionCount
        }


        companion object {

            fun getNumberOfAnsweredQuestions(quiz: Quiz) = getAnsweredIndexes(
                quiz
            ).size

            fun getNumberOfCorrectAnswers(quiz: Quiz) = getCorrectlyAnsweredIndexes(
                quiz
            ).size
            /**
             * Retrieves the indexes for all questions that has been answered
             */
            fun getAnsweredIndexes(quiz: Quiz): List<Int> {
                return (0 until quiz.selectionState.size).filter { quiz.getResult(it) != -1 }.map { it }
            }

            /**
             * Retrieves the indexes for all the correctly answered questions
             */
            fun getCorrectlyAnsweredIndexes(quiz: Quiz): List<Int> {
                return getAnsweredIndexes(quiz).filter { quiz.getResult(it) == 1 }
                    .map { it }
            }
        }
    }

    companion object {

        private var instance: QuizHistory? = null

        fun getInstance(context: Context): QuizHistory {
            if (instance == null){
                instance =
                    QuizHistory(context)
            }
            return instance!!
        }

        fun saveToBundle(quiz: Quiz?, outState: Bundle?, maker: String? = this::class.java.name) {
            outState?.apply {
                quiz?.also {
                    // save quiz id
                    putLong("quiz_id", it.id)

                    // save history data
                    putIntegerArrayList("quiz_index", it.questionIndexes)
                    putIntegerArrayList("quiz_init", it.initStates)
                    putIntegerArrayList("quiz_select", it.selectionState)
                    putString("quiz_topic", it.topic)
                    putString("quiz_maker", maker)
                    putInt("quiz_pointer", it.currentIndex)
                }
            }
        }

        fun saveToBundle(hComponent: HistoryComponent?, outState: Bundle?) {
            saveToBundle(
                hComponent?.getQuiz(),
                outState,
                hComponent?.let {
                    it::class.java.name
                })
            hComponent?.saveToBundle(outState)
        }

        fun getBundleMaker(inState: Bundle?): String? = inState?.getString("quiz_maker")

        fun getHistoryFromBundle(inState: Bundle) : History? {
            return if (inState.containsKey("quiz_id"))
                inState.run {
                    History(
                        getString("quiz_topic") ?: "", getLong("quiz_id"),
                        getIntegerArrayList("quiz_index") ?: arrayListOf(),
                        getIntegerArrayList("quiz_init") ?: arrayListOf(),
                        getIntegerArrayList("quiz_select") ?: arrayListOf(),
                        getInt("quiz_pointer")
                    )

                }
            else null
        }

        fun restoreState(quiz: Quiz, inState: Bundle?, timeStamp: Long? = null, isTemporal: Boolean = false) {
            (inState?.let {
                getHistoryFromBundle(it)
            }?: timeStamp?.let {
                getInstance(quiz.getContext())
                    .getHistory(it, isTemporal)
            })?.also {
                quiz.apply {
                    id = it.timeStamp
                    topic = it.topic
                    initStates = ArrayList<Int>().apply { addAll(it.initS)}
                    selectionState = ArrayList<Int?>().apply { addAll(it.selectS)}
                    currentIndex = it.pointer?:0
                    questionIndexes = ArrayList<Int>().apply { addAll(it.qIndexes)}
                }
            }
        }

        fun restoreState(hComponent: HistoryComponent, inState: Bundle?, timeStamp: Long? = null, isTemporal: Boolean = false) {
            restoreState(
                hComponent.getQuiz(),
                inState,
                timeStamp,
                isTemporal
            )

            hComponent.restoreState(inState, timeStamp, isTemporal)
        }
    }
}