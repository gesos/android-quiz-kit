package com.orsteg.androidquizkit

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences


class QuizHistory private constructor(context: Context) {

    private val pref = context.getSharedPreferences("quiz_history", Activity.MODE_PRIVATE)
    private val editor = pref.edit()

    fun getAllStats(): List<Stats> = pref.getStringSet("quiz_ids")
            .map {
                pref.run {
                    Stats(getString("${it}_topic", "")?:"", it.toLong(), getStringSet("${it}_answers").toIntList(),
                        getStringSet("${it}_correct").toIntList(), getInt("${it}_count", 0),
                        getNullableLong("${it}_total_time"), getNullableLong("${it}_finish_time"))
                }
            }


    fun getStat(it: Long): Stats?{
        return if (pref.getStringSet("quiz_ids").contains(it.toString())) {
            pref.run {
                Stats(
                    getString("${it}_topic", "")?:"", it, getStringSet("${it}_answers").toIntList(),
                    getStringSet("${it}_correct").toIntList(), getInt("${it}_count", 0),
                    getNullableLong("${it}_total_time"), getNullableLong("${it}_finish_time")
                )
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
                .remove("$head${it}_init")
                .remove("$head${it}_topic")
                .remove("$head${it}_select").apply()

            if (isTemporal) {
                editor.remove("$head${it}_pointer").apply()
            } else {
                // remove stats
                editor.remove("${it}_count").remove("${it}_answers")
                    .remove("${it}_correct").remove("${it}_total_time")
                    .remove("${it}_finish_time").apply()
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
                    getString("$head${it}_topic", "")?:"", it, getStringSet("$head${it}_index").toIntList(),
                    getStringSet("$head${it}_init").toIntList(), getStringSet("$head${it}_select").toIntList(),
                    getNullableInt("$head${it}_pointer")
                )
            }
        } else null
    }

    fun saveToHistory(quiz: Quiz, isTemporal: Boolean = false) {
        val head = if (isTemporal) "temp_" else ""

        // save history id
        editor.putStringSet("${head}quiz_ids",
            pref.getStringSet("${head}quiz_ids").toMutableSet()
                .plus("${quiz.id}")).apply()

        // save history data
        editor.putStringSet("$head${quiz.id}_index", quiz.questionIndexes.toStringSet())
            .putStringSet("$head${quiz.id}_init", quiz.initStates.toStringSet())
            .putStringSet("$head${quiz.id}_select", quiz.selectionState.toStringSet())
            .putString("$head${quiz.id}_topic", quiz.topic).apply()

        if (isTemporal) {
            editor.putInt("$head${quiz.id}_pointer", quiz.currentSet).apply()
        } else {
            // generate stats
            val stats = Stats(quiz)

            editor.putInt("${quiz.id}_count", stats.questionCount)
                .putStringSet("${quiz.id}_answers", stats.answeredIndexes?.toStringSet())
                .putStringSet("${quiz.id}_correct", stats.correctIndexes?.toStringSet())
                .putLong("${quiz.id}_total_time", stats.totalTime?:-1L)
                .putLong("${quiz.id}_finish_time", stats.finishTime?:-1L).apply()
        }

        editor.commit()
    }

    private fun <T> List<T>.toStringSet() = this.map { it.toString() }.toSet()
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

    class History(val topic: String, val timeStamp: Long, val qIndexes: List<Int>, val initS: List<Int>, val selectS: List<Int>, val pointer: Int?)

    class Stats private constructor() {

        var topic: String = ""
        var quizTimestamp: Long = 0
        var answeredIndexes: List<Int>? = null
        var answeredCount: Int = 0
        var correctIndexes: List<Int>? = null
        var correctCount: Int = 0
        var questionCount: Int = 0
        var totalTime: Long? = null
        var finishTime: Long? = null
        
        constructor(quiz: Quiz): this() {
            topic = quiz.topic
            quizTimestamp = quiz.id
            answeredIndexes = getAnsweredIndexes(quiz)
            answeredCount = answeredIndexes?.size?:0
            correctIndexes = getCorrectlyAnsweredIndexes(quiz)
            correctCount = correctIndexes?.size?:0
            questionCount = quiz.getTotalQuestions()
            totalTime = 0
            finishTime = 0
        }

        constructor(topic: String, quizTimestamp: Long, answeredIndexes: List<Int>, correctIndexes: List<Int>,
                    questionCount: Int, totalTime: Long?, finishTime: Long?): this() {
            this.topic = topic
            this.quizTimestamp = quizTimestamp
            this.answeredIndexes = answeredIndexes
            answeredCount = answeredIndexes.size
            this.correctIndexes = correctIndexes
            correctCount = correctIndexes.size
            this.questionCount = questionCount
            this.totalTime = totalTime
            this.finishTime = finishTime
        }

        companion object {

            fun getNmberOfAnsweredQuestions(quiz: Quiz) = getAnsweredIndexes(quiz).size

            fun getNmberOfCorrectAnswers(quiz: Quiz) = getCorrectlyAnsweredIndexes(quiz).size
            /**
             * Retrieves the indexes for all questions that has been answered
             */
            fun getAnsweredIndexes(quiz: Quiz): List<Int> {
                return (0 until quiz.selectionState.size).filter { quiz.selectionState[it] != -1 }.map { it }
            }

            /**
             * Retrieves the indexes for all the correctly answered questions
             */
            fun getCorrectlyAnsweredIndexes(quiz: Quiz): List<Int> {
                return (0 until quiz.selectionState.size).filter { quiz.selectionState[it] == quiz.getQuestion(it).answer }.map { it }
            }

        }
    }

    companion object {

        private var instance: QuizHistory? = null

        fun getInstance(context: Context): QuizHistory {
            if (instance == null){
                instance = QuizHistory(context)
            }
            return instance!!
        }
    }
}