package com.orsteg.gesos.androidquizkit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class TestActivity: AppCompatActivity() {

    lateinit var mQuiz: TimedQuiz

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val builder : QuizBuilder = QuizBuilder.Builder(this).apply {
            getDefaultQuizParser().apply {

            }
        }.build("physics", BuildMethod.fromString(""))


        val quizConfig : Quiz.Config = Quiz.Config().
            randomizeOptions().
            randomizeQuestions().
            setCount(30).
            maxSetSize(5)

        builder.getQuiz(quizConfig, object : Quiz.OnBuildListener {
            override fun onFinishBuild(quiz: Quiz) {
                // not on UI thread
                mQuiz = TimedQuiz(quiz, 6000)
                QuizHistory.restoreState(mQuiz, savedInstanceState)
                mQuiz.getCurrentQuestionSet()
            }
        })

    }

    fun startQuiz() {
        mQuiz.apply {
            onTimeChangeListener = object : TimedQuiz.OnTimeChangeListener {
                override fun onTimerTick(passedTimeInMillis: Long) {
                    // not on UI thread
                    // update time TextView
                }

                override fun onTimerFinish() {
                    // not on UI thread
                    endQuiz()
                }
            }

            getCurrentQuestionSet()

            start()
        }
    }

    fun endQuiz() {

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        QuizHistory.saveToBundle(mQuiz, outState)
    }
}