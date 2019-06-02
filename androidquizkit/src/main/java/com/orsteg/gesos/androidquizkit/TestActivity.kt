package com.orsteg.gesos.androidquizkit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class TestActivity: AppCompatActivity() {

    var mQuiz: TimedQuiz? = null

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
                val timedQuiz = TimedQuiz(quiz, 6000)
                QuizHistory.restoreState(timedQuiz, savedInstanceState)
                quiz.getCurrentQuestionSet()
            }
        })

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        QuizHistory.saveToBundle(mQuiz, outState)
    }
}