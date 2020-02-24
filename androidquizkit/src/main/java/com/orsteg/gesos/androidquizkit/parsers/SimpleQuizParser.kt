package com.orsteg.gesos.androidquizkit.parsers

import android.util.Log
import com.orsteg.gesos.androidquizkit.quiz.Question

class SimpleQuizParser: BaseQuizParser() {

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

    init {
        // Fix for the buffer, since parsing always starts from a new line
        if (!shouldValidate) mBuffer.append("\n")
    }

    override fun validate(): Boolean {

        Log.d("tg", mBuffer.substring(0, headerByteSize))
        val result = mBuffer.substring(0, headerByteSize) == "<!QUIZ>"

        if (result) mPointer = mBuffer.indexOf(">")
        return result
    }

    override fun parse(pointer: Int): Int {
        Log.d("tg", "parse $pointer")

        var cursor = pointer

        var start: Int
        var end = 0

        while (end != -1 && cursor != mBuffer.length) {
            Log.d("tg", "parsing $pointer")

            start = mBuffer.indexOf("\n", cursor)
            end = mBuffer.indexOf("\n", start + 1)

            if (end != -1 || mCallerState == State.END_OT_READ) {

                if (mCallerState == State.END_OT_READ && end == -1) {
                    end = mBuffer.length
                }

                val text = mBuffer.substring(start + 1, end)

                // check for valid file line
                if (!text.matches("\\s*".toRegex())) {

                    // Each 5th line starting from 0 represents the beginning of a new statement
                    // While each 1st to 4th lines starting from 0 contains the options a - d
                    val line = currentLine % (mOptionsCount + 1)
                    if (line == 0) {


                        // set the statement text
                        question.statement = text.replaceFirst("(\\s*)(\\d+)(\\.)(\\s*)".toRegex(), "")

                    } else if (line in 1..mOptionsCount) {

                        // check if current option is the answer
                        val isAnswer = text.matches("(\\s*)(\\*)([abcd]+)(.*)".toRegex())

                        // get the option removing the options letter
                        val option = text.replaceFirst("(\\s*)([*abcd]+)(\\.)(\\s*)".toRegex(), "")

                        // add the option to the statement
                        question.options.add(option)

                        if (isAnswer) {
                            question.answer = question.options.size - 1
                        }

                        // check if the last option has been included to the statement and add the statement to the ArrayList
                        if (line == mOptionsCount) {

                            // add statement to ArrayList
                            tempQuestions.add(question)

                            // init a new statement
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
