package com.orsteg.androidquizkit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class TestActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val builder : QuizBuilder = QuizBuilder.Builder(this).apply {
            getDefaultQuizParser().apply {

            }
        }.build(BuildMethod.fromString(""))


        val quizConfig : Quiz.Config = Quiz.Config().
            randomizeOptions().
            randomizeQuestions().
            setCount(30).
            maxSetSize(5)

        builder.getQuiz(quizConfig, object : Quiz.OnBuildListener {
            override fun onFinishBuild(quiz: Quiz) {
                quiz.getCurrentQuestionSet()
            }
        })

    }

}