package com.orsteg.gesos.androidquizkit.quizParser

import com.orsteg.gesos.androidquizkit.BaseQuizParser
import com.orsteg.gesos.androidquizkit.Question
import com.orsteg.gesos.androidquizkit.QuizBuilder

class QuizParser: BaseQuizParser() {

    override val headerByteSize: Int = 7

    private var currentLine: Int = 0

    var question = Question()

    val tempQuestions = ArrayList<Question>()

    var mAnswerMarker: String = "*"
        set(value) {
            if (value.isNotEmpty())field = value
        }

    var mOptionsCount: Int = 4
        set(value) {
            if (value > 0) field = value
        }

    override fun validate(): Boolean {

        return mBuffer.substring(0, headerByteSize) == "<!QUIZ>"
    }

    override fun parse(pointer: Int): Int {

        var cursor = pointer
        if (cursor == 0) cursor = mBuffer.indexOf(">")

        var start: Int
        var end = 0

        while (end != -1 && cursor != mBuffer.lastIndex) {

            start = mBuffer.indexOf("\n", cursor)
            end = mBuffer.indexOf("\n", start + 1)

            if (end != -1 || mState == State.END_OT_DATA) {

                if (mState == State.END_OT_DATA) {
                    end = mBuffer.length
                }

                val text = mBuffer.substring(start + 1, end)

                // check for valid file line
                if (!text.matches("\\s*".toRegex())) {
                    // Each 5th line starting from 0 represents the beginning of a new question
                    // While each 1st to 4th lines starting from 0 contains the options a - d
                    val line = currentLine % mOptionsCount + 1
                    if (line == 0) {

                        // set the question text
                        question.question = text.replaceFirst("(\\s*)(\\d+)(\\.)(\\s*)".toRegex(), "")

                    } else if (line in 1..mOptionsCount) {

                        // check if current option is the answer
                        val isAnswer = text.matches("(\\s*)(\\*)([abcd]+)(.*)".toRegex())

                        // get the option removing the options letter
                        val option = text.replaceFirst("(\\s*)([*abcd]+)(\\.)(\\s*)".toRegex(), "")

                        // add the option to the question
                        question.options.add(option)

                        if (isAnswer) question.answer = question.options.size - 1

                        // check if the last option has been included to the question and add the question to the ArrayList
                        if (line == mOptionsCount) {

                            // add question to ArrayList
                            tempQuestions.add(question)

                            // init a new question
                            question = Question()
                        }
                    }

                    currentLine++
                }

                cursor = end
            }
        }

        return cursor
    }

    override fun getQuestions(): List<Question>? {
        return tempQuestions
    }


}