package com.orsteg.gesos.androidquizkit

import android.os.Bundle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

abstract class Quiz (var topic: String, private var mConfig: Config): QuizController{

    var id: Long = 0

    var currentSet: Int = -1

    // State variables
    var questionIndexes: ArrayList<Int> = ArrayList()
    var initStates: ArrayList<Int> = ArrayList()
    var selectionState: ArrayList<Int?> = ArrayList()


    abstract fun setupQuiz()

    abstract fun getTotalQuestions(): Int

    fun setId() {
        id = Calendar.getInstance().timeInMillis
    }

    // Moves a pointer
    override fun getCurrentQuestionSet(): List<Question>  {
        return gotoQuestionSet(currentSet)
    }

    override fun nextQuestionSet(): List<Question>  {
        return gotoQuestionSet(++currentSet)
    }

    override fun previousQuestionSet(): List<Question> {
        return gotoQuestionSet(--currentSet)
    }

    override fun gotoQuestionSet(set: Int): List<Question>  {
        val size = if (mConfig.mSetSize < 0) getTotalQuestions()
                    else mConfig.mSetSize

        val i = set * size
        currentSet = set

        if (id == 0L) setId()

        return getQuestionRange(i, min(i+size, getTotalQuestions() - 1))
    }

    // Does not move pointer
    fun getQuestionRange(startIndex: Int, stopIndex: Int) : List<Question> {
        return getQuestions((startIndex..stopIndex).toList())
    }

    fun getQuestions(indexes: List<Int>): List<Question> {
        return indexes.map { i -> getQuestion(i) }
    }

    fun getQuestion(index: Int): Question {
        val q = fetchQuestion(index)
        if (!q.hasInit) q.apply {
            key = index
            init(mConfig, initStates[index])
            hasInit = true
        }

        return q
    }

    protected abstract fun fetchQuestion(index: Int): Question

    // Config functions
    fun generateRandomStates() = Random().run { (0 until resolveQuestionCount()).map { this.nextInt(11) } }

    fun generateRandomIndexes() = (0 until getTotalQuestions()).toMutableList().apply {
        shuffle()
    }.subList(0, resolveQuestionCount()).apply {
        shuffle()
    }.toList()

    fun generateIndexes() = (0 until getTotalQuestions()).toMutableList().subList(0,
        resolveQuestionCount()).toList()

    private fun resolveQuestionCount() = min(kotlin.run {
        val n = mConfig.mQuestionCount
        if (n < 0) getTotalQuestions() - 1
        else mConfig.mQuestionCount
    }, getTotalQuestions() - 1)

    // Methods to help determine ranges
    fun getSetCount() = getTotalQuestions() / getSetSize()

    fun getSetSize() = mConfig.mSetSize

    fun getLastSetSize() = (getTotalQuestions() % getSetSize()).let { if (it == 0) getSetSize() else it }

    fun getSetForQuestionIndex(index: Int) = index / getSetSize()



    interface QuizInterface {
        fun getQuiz(config: Config, listener: OnBuildListener)
    }

    interface OnBuildListener {
        fun onFinishBuild(quiz: Quiz)
    }

    class Config {

        var mRandomizeOptions: Boolean = true
        var mRandomizeQuestions: Boolean = true
        var mQuestionCount: Int = -1
        var mSetSize: Int = 1

        fun randomizeOptions(randomize: Boolean = true): Config {
            mRandomizeOptions = randomize
            return this
        }
        fun randomizeQuestions(randomize: Boolean = true): Config {
            mRandomizeQuestions = randomize
            return this
        }
        fun setCount(count: Int): Config {
            mQuestionCount = count
            return this
        }
        fun maxSetSize(size: Int): Config {
            mSetSize = size
            return this
        }
    }
}

