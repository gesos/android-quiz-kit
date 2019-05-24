package com.orsteg.androidquizkit.quizParser

import com.orsteg.androidquizkit.BaseQuizParser
import com.orsteg.androidquizkit.Question

class QuizParser: BaseQuizParser() {

    override val headerByteSize: Int = 20

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