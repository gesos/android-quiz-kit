package com.orsteg.gesos.androidquizkit

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

abstract class Quiz (var topic: String, private var mConfig: Config): QuizController{

    var id: Long = 0

    var currentGroup: Int = 0

    // State variables
    var questionIndexes: ArrayList<Int> = ArrayList()
    var initStates: ArrayList<Int> = ArrayList()
    var selectionState: ArrayList<Int?> = ArrayList()


    abstract fun setupQuiz()

    abstract fun getTotalQuestions(): Int

    abstract fun getContext(): Context

    fun setId() {
        id = Calendar.getInstance().timeInMillis
    }

    fun getTotalQuizQuestions(): Int = resolveQuestionCount()

    // Moves a pointer
    override fun getCurrentQuestionGroup(): List<Question>  {
        return gotoQuestionGroup(currentGroup)
    }

    override fun nextQuestionGroup(): List<Question>  {
        return gotoQuestionGroup(++currentGroup)
    }

    override fun previousQuestionGroup(): List<Question> {
        return gotoQuestionGroup(--currentGroup)
    }

    override fun gotoQuestionGroup(group: Int): List<Question>  {
        val size = if (mConfig.groupSize < 0) getTotalQuizQuestions()
                    else mConfig.groupSize

        val i = group * size
        currentGroup = group

        if (id == 0L) setId()

        return getQuestionRange(i, min(i+size, getTotalQuizQuestions() - 1))
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

    fun Question.setSelection() {

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
        val n = mConfig.questionCount
        if (n < 0) getTotalQuestions() - 1
        else mConfig.questionCount
    }, getTotalQuestions() - 1)

    // Methods to help determine ranges
    fun getGroupCount() = getTotalQuizQuestions() / getGroupSize()

    fun getGroupSize() = mConfig.groupSize

    fun getLastGroupSize() = (getTotalQuizQuestions() % getGroupSize()).let { if (it == 0) getGroupSize() else it }

    fun getGroupForQuestionIndex(index: Int) = index / getGroupSize()



    interface QuizInterface {
        fun getQuiz(config: Config, listener: OnBuildListener)
    }

    interface OnBuildListener {
        fun onFinishBuild(quiz: Quiz)
    }

    class Config {

        var randomizeOptions: Boolean = true
        var randomizeQuestions: Boolean = true
        var questionCount: Int = -1
        var groupSize: Int = 1

        fun randomizeOptions(randomize: Boolean = true): Config {
            randomizeOptions = randomize
            return this
        }
        fun randomizeQuestions(randomize: Boolean = true): Config {
            randomizeQuestions = randomize
            return this
        }
        fun setCount(count: Int): Config {
            questionCount = count
            return this
        }
        fun maxGroupSize(size: Int): Config {
            groupSize = size
            return this
        }
    }
}

