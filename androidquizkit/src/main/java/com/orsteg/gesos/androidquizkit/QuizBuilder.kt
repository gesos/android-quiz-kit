package com.orsteg.gesos.androidquizkit

import android.app.Activity
import android.content.Context
import android.util.Log
import com.orsteg.gesos.androidquizkit.quizParser.QuizParser
import kotlinx.coroutines.*
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
                    buildFromInputStreamAsync(this)
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
                buildFromInputStreamAsync(context.resources.openRawResource(this))
            }

    }


    private fun buildFromInputStreamAsync(quizStream: InputStream) = runBlocking {

        GlobalScope.async {
            val buffer = BufferedInputStream(quizStream, 8192)

            // Holds the number of bytes read for each loop
            var count = 0
            val data = ByteArray(1024)
            var parser = mBuilder.getQuizParser()?: mBuilder.getDefaultQuizParser()
            var response: BaseQuizParser.State = BaseQuizParser.State.IDLE

            try {
                Log.d("tg", "read start")

                loop@ while (!arrayOf(BaseQuizParser.State.FORCE_FINISH, BaseQuizParser.State.PARSE_ERROR,
                        BaseQuizParser.State.VALIDATION_FAILED, BaseQuizParser.State.PARSE_SUCCESS).contains(response)) {

                    if (count != -1) {
                        count = buffer.read(data)
                    }
                    response = if (count != -1) {
                        parser.append(data, count)
                    } else {
                        parser.finish()
                    }

                    if (response == BaseQuizParser.State.CHANGE_PARSER) {
                        if (mBuilder.getQuizParser() == null) {

                            // Code to choose new parser
                            //val parserInfo = parser.mNewParser

                            parser.cancel()

                            val new = QuizParser()
                            parser.copy(new)

                            parser = new
                        }
                    }
                }


                Log.d("tg", "read finish")

                if (arrayOf(
                        BaseQuizParser.State.FORCE_FINISH,
                        BaseQuizParser.State.PARSE_SUCCESS
                    ).contains(response)) {
                    Log.d("tg", "parse finish success")

                    parser.getQuestions()?.apply { questions = this }

                    (context as Activity).runOnUiThread {
                        notifyListener()
                    }

                } else {
                    Log.d("tg", "parse finish fail${parser.mState}")

                }

                if (mBuilder.getQuizParser() == null) {
                    mBuilder.setQuizParser(parser)
                }

            } catch (e: IOException) {
                Log.d("Error", e.message)
            }

            buffer.close()
            quizStream.close()

        }
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

        override fun getContext(): Context = context
    }

}