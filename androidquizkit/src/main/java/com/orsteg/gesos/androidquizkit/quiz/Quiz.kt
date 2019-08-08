package com.orsteg.gesos.androidquizkit.quiz

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

abstract class Quiz (var topic: String, private var mConfig: Config):
    QuizController {

    var id: Long = 0

    var currentIndex: Int = 0

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

    override fun getQuestionCount(): Int {
        return getTotalQuizQuestions()
    }

    override fun getCurrentQuestionIndex(): Int {
        return currentIndex
    }

    // Moves a pointer
    override fun getCurrentQuestion(): Question {
        return gotoQuestion(currentIndex)
    }

    override fun nextQuestion(): Question {
        return gotoQuestion(++currentIndex)
    }

    override fun previousQuestion(): Question {
        return gotoQuestion(--currentIndex)
    }

    override fun gotoQuestion(index: Int): Question {
        return getQuestion(index)
    }

    override fun setSelection(index: Int, selection: Int?) {
        selectionState[index] = selection
    }

    override fun setSelection(selection: Int?) {
        setSelection(currentIndex, selection)
    }

    override fun getSelection(index: Int): Int? {
        return selectionState[index]
    }

    override fun getSelection(): Int? {
        return getSelection(currentIndex)
    }

    override fun getResult(index: Int): Int {
        return when (getSelection(index)) {
            getQuestion(index).answer -> 1
            null -> -1
            else -> 0
        }
    }

    override fun getResult(): Int {
        return getResult(currentIndex)
    }

    fun getQuestion(index: Int): Question {
        if (id == 0L) setId()
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
        val n = mConfig.questionCount
        if (n < 0) getTotalQuestions()
        else mConfig.questionCount
    }, getTotalQuestions())


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
    }
}

