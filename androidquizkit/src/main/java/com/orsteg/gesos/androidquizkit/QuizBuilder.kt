package com.orsteg.gesos.androidquizkit

import android.content.Context
import android.util.Log
import com.orsteg.gesos.androidquizkit.quizParser.QuizParser
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream

class QuizBuilder private constructor(private val context: Context, private val topic: String, private val mBuilder: Builder,
                                      private val mMethod: BuildMethod): Quiz.QuizInterface {

    // Holds all the questions for the quiz
    var questions: List<Question> = listOf()

    // Quiz cache
    private var mQuiz: Quiz? = null
    private var mBuildListener: Quiz.OnBuildListener? = null


    init {
        when (mMethod.mMethod) {
            BuildMethod.Method.RES -> {
                buildFromResource()
            }
            BuildMethod.Method.STREAM -> {
                mMethod.mStream?.apply {
                    buildFromInputStream(this)
                }
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

        Thread {
            val buffer = BufferedInputStream(quizStream, 8192)

            // Holds the number of bytes read for each loop
            var count: Int
            val data = ByteArray(1024)
            var parser = mBuilder.getQuizParser()?: mBuilder.getDefaultQuizParser()

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
                            if (mBuilder.getQuizParser() == null) {

                                // Code to choose new parser
                                //val parserInfo = parser.mNewParser

                                parser.cancel()

                                val new = QuizParser()
                                parser.copy(new)

                                parser = new
                            }
                        }
                        else -> {
                            continue@loop
                        }
                    }

                }

                if (arrayOf(
                        BaseQuizParser.State.FORCE_FINISH,
                        BaseQuizParser.State.PARSE_SUCCESS
                    ).contains(parser.finish())) {

                    parser.getQuestions()?.apply { questions = this }

                    notifyListener()

                } else {

                }

                if (mBuilder.getQuizParser() == null) {
                    mBuilder.setQuizParser(parser)
                }

            } catch (e: IOException) {
                Log.d("Error", e.message)
            }

            buffer.close()
            quizStream.close()

        }.start()
    }

    private fun notifyListener() {
        mBuildListener?.apply {

            mQuiz?.apply {
                setupQuiz()
                onFinishBuild(this)
            }
        }
    }

    fun cancel() {

    }

    fun rebuild() {

    }


    override fun getQuiz(config: Quiz.Config, listener: Quiz.OnBuildListener) {
        mQuiz = Quiz1(topic, config)
        mBuildListener = listener

        if (questions.isNotEmpty()) {
            notifyListener()
        }
    }


    class Builder(private val context: Context) {

        private val defQuizParser: QuizParser = QuizParser()
        private var mQuizParser: BaseQuizParser? = null

        fun getQuizParser(): BaseQuizParser? = mQuizParser

        fun getDefaultQuizParser(): QuizParser = defQuizParser

        fun setQuizParser(parser: BaseQuizParser): Builder {
            mQuizParser = parser
            return this
        }

        fun build(topic: String, method: BuildMethod): QuizBuilder {
            return QuizBuilder(context, topic, this, method)
        }
    }


    private inner class Quiz1(topic: String,val config: Config) : Quiz(topic, config) {

        override fun setupQuiz() {
            questionIndexes.addAll(run { if (config.mRandomizeQuestions) generateRandomIndexes()
                else generateIndexes()})
            initStates.addAll(generateRandomStates())
            selectionState.addAll((0 until getTotalQuestions()).map { null })
        }

        override fun getTotalQuestions(): Int {
            return questions.size
        }

        override fun fetchQuestion(index: Int): Question {
            return questions[questionIndexes[index]]
        }

    }

}