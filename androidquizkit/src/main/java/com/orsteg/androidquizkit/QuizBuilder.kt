package com.orsteg.androidquizkit

import java.util.*

class QuizBuilder private constructor(private var mBuilder: Builder, private var mMethod: BuildMethod): Quiz.QuizInterface {

    // Holds all the questions for the quiz
    var questions: ArrayList<Question> = ArrayList()

    // Quiz config cache
    private var mQuiz: Quiz? = null
    private var mBuildListener: Quiz.OnBuildListener? = null


    override fun getQuiz(config: Quiz.Config, listener: Quiz.OnBuildListener) {
        mQuiz = Quiz1(config)
        mBuildListener = listener
    }


    class Builder {
        var mAnswerMarker: String = "*"
            private set(value) {
                field = value
            }

        var mOptionsCount: Int = 4
            private set(value) {
                field = value
            }

        fun setAnswerMarker(marker: String): Builder {
            return this
        }

        fun setOptionsCount(count: Int): Builder {
            return this
        }

        fun build(method: BuildMethod): QuizBuilder {
            return QuizBuilder(this, method)
        }
    }


    private inner class Quiz1(config: Config) : com.orsteg.androidquizkit.Quiz(config) {

        fun setupQuiz() {

        }

        override fun getTotalQuestions(): Int {
            return questions.size
        }

        override fun getCurrentQuestionSet() {

        }

        override fun nextQuestionSet() {

        }

        override fun previousQuestionSet() {

        }

        override fun gotoQuestionSet(set: Int) {

        }

        override fun getQuestionRange(startIndex: Int, stopIndex: Int) {

        }

        override fun getQuestions(indexes: List<Int>) {

        }

        override fun getQuestion(index: Int) {

        }

    }

}