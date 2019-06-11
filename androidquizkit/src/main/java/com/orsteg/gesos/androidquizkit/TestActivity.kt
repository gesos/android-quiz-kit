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
            maxGroupSize(5)

        builder.getQuiz(quizConfig, object : Quiz.OnBuildListener {
            override fun onFinishBuild(quiz: Quiz) {

                quiz.apply {

                }
                mQuiz = TimedQuiz(quiz, 6000)
                QuizHistory.restoreState(mQuiz, savedInstanceState)
                startQuiz()
            }
        })

    }

    fun startQuiz() {
        mQuiz.apply {
            onTimeChangeListener = object : TimedQuiz.OnTimeChangeListener {
                override fun onTimerTick(timeLeft: Long) {
                    // update time TextView
                }

                override fun onTimerFinish() {
                    endQuiz()
                }
            }

            getCurrentQuestionGroup()[0]

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