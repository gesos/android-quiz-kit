package com.orsteg.androidquizkit

import android.content.Context
import android.util.Log
import com.orsteg.androidquizkit.quizParser.QuizParser
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.StringBuilder
import java.util.*

class QuizBuilder private constructor(private val context: Context, private val mBuilder: Builder,
                                      private val mMethod: BuildMethod): Quiz.QuizInterface {

    // Holds all the questions for the quiz
    var questions: ArrayList<Question> = ArrayList()

    // Quiz config cache
    private var mQuiz: Quiz? = null
    private var mBuildListener: Quiz.OnBuildListener? = null


    init {
        when (mMethod.mMethod) {
            BuildMethod.Method.RES -> {
                buildFromResource()
            }
            BuildMethod.Method.STREAM -> {

            }
            BuildMethod.Method.STR -> {

            }
            BuildMethod.Method.FILE -> {

            }
            BuildMethod.Method.URL -> {

            }
        }
    }

    private fun buildFromResource() {
        mMethod.mRes?.apply {
            buildFromInputStream(context.resources.openRawResource(this))
        }
    }


    private fun buildFromInputStream(quizStream: InputStream) {

        val buffer = BufferedInputStream(quizStream, 8192)

        // Holds the number of bytes read for each loop
        var count = 0

        val data = ByteArray(1024)

        var parser: BaseQuizParser = mBuilder.mQuizParser ?: QuizParser(mBuilder)

        try {

            loop@ while (run { count = buffer.read(data); count } != -1) {


                when (parser.append(data)) {
                    BaseQuizParser.State.FORCE_FINISH -> {
                        break@loop
                    }
                    BaseQuizParser.State.PARSE_ERROR -> {
                        break@loop
                    }
                    BaseQuizParser.State.VALIDATION_FAILED -> {
                        break@loop
                    }
                    BaseQuizParser.State.CHANGE_PARSER -> {
                        if (mBuilder.mQuizParser == null) {

                            val parserInfo = parser.mNewParser
                            // Code to choose new parser

                            parser.cancel()

                            val new = QuizParser(mBuilder)
                            parser.copy(new)

                            parser = new
                        }
                    }
                    else -> {
                        continue@loop
                    }
                }

            }

            if (arrayOf(BaseQuizParser.State.FORCE_FINISH, BaseQuizParser.State.PARSE_SUCCESS).contains(parser.finish())) {

                val questions = parser.getQuestions()

            } else {

            }

            if (mBuilder.mQuizParser == null) {
                mBuilder.mQuizParser = parser
            }

        } catch (e: IOException) {
            Log.d("Error", e.message)
        }

        buffer.close()
        quizStream.close()

    }

    fun cancel() {

    }

    fun rebuild() {

    }


    override fun getQuiz(config: Quiz.Config, listener: Quiz.OnBuildListener) {
        mQuiz = Quiz1(config)
        mBuildListener = listener
    }


    class Builder(private val context: Context) {
        var mAnswerMarker: String = "*"
            private set(value) {
                field = value
            }

        var mOptionsCount: Int = 4
            private set(value) {
                field = value
            }

        var mQuizParser: BaseQuizParser? = null

        fun setAnswerMarker(marker: String): Builder {
            return this
        }

        fun setOptionsCount(count: Int): Builder {
            return this
        }

        fun setQuizParser(parser: BaseQuizParser): Builder {
            return this
        }

        fun build(method: BuildMethod): QuizBuilder {
            return QuizBuilder(context, this, method)
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