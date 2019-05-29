package com.orsteg.androidquizkit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class TestActivity: AppCompatActivity() {

    var mQuiz: Quiz? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val builder : QuizBuilder = QuizBuilder.Builder(this).apply {
            getDefaultQuizParser().apply {

            }
        }.build(BuildMethod.fromString(""))


        val quizConfig : Quiz.Config = Quiz.Config(savedInstanceState).
            randomizeOptions().
            randomizeQuestions().
            setCount(30).
            maxSetSize(5)

        val timer: QuizTimer = object : QuizTimer(60000) {
            override fun onTimerTick(passedTimeInMillis: Long) {
                // update TextView
            }

            override fun onTimerFinish() {
                // end quiz
            }
        }

        builder.getQuiz(quizConfig, object : Quiz.OnBuildListener {
            override fun onFinishBuild(quiz: Quiz) {
                QuizHistory.restoreState(quiz, savedInstanceState)
                mQuiz = quiz
                quiz.getCurrentQuestionSet()
            }
        })

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        QuizHistory.saveToBundle(mQuiz, outState)
    }
}