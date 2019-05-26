package com.orsteg.androidquizkit.quizParser

import com.orsteg.androidquizkit.BaseQuizParser
import com.orsteg.androidquizkit.Question
import com.orsteg.androidquizkit.QuizBuilder

class QuizParser(): BaseQuizParser() {

    override val headerByteSize: Int = 20

    var mAnswerMarker: String = "*"
        private set(value) {
            field = value
        }

    var mOptionsCount: Int = 4
        private set(value) {
            field = value
        }

    override fun validate(): Boolean {

        return false
    }

    override fun parse(pointer: Int): Int {

        return 0
    }

    override fun getQuestions(): Array<Question>? {

        return null
    }


}