package com.orsteg.androidquizkit

import java.util.*

abstract class Quiz (private var mConfig: Config){

    var currentSet: Int = -1
    //private val hasState = savedState != null

    // State variables
    var randomIndexes: ArrayList<Int> = ArrayList()
    var selections: ArrayList<Int> = ArrayList()


    var rnd: Random = Random()

    abstract fun getTotalQuestions(): Int


    // Moves a pointer
    abstract fun getCurrentQuestionSet()
    abstract fun nextQuestionSet()
    abstract fun previousQuestionSet()
    abstract fun gotoQuestionSet(set: Int)

    // Does not move pointer
    abstract fun getQuestionRange(startIndex: Int, stopIndex: Int)
    abstract fun getQuestions(indexes: List<Int>)
    abstract fun getQuestion(index: Int)

    // Config functions
    protected fun selectRandomQuestions() {

    }

    private fun selectRandomOptions() {

    }

    // Methods to help determine ranges
    fun getSetCount() {

    }
    fun getSetSize() {

    }
    fun getLastSetSize() {

    }
    fun getSetForQuestionIndex() {

    }



    interface QuizInterface {
        fun getQuiz(config: Config, listener: OnBuildListener)
    }

    interface OnBuildListener {
        fun onFinishBuild(quiz: Quiz)
    }

    class Config {

        private var mRandomizeOptions: Boolean = true
        private var mRandomizeQuestions: Boolean = true
        private var mQuestionCount: Int = -1
        private var mSetSize: Int = 1
        private var mTimeInSeconds = -1

        fun randomizeOptions(randomize: Boolean = true): Config {
            return this
        }
        fun randomizeQuestions(randomize: Boolean = true): Config {
            return this
        }
        fun setCount(count: Int): Config {
            return this
        }
        fun maxSetSize(size: Int): Config {
            return this
        }
        fun setTimer(timeInSeconds: Int): Config {
            return this
        }
    }
}

