package com.orsteg.gesos.androidquizkit

import android.app.Activity
import android.content.Context
import android.util.Log
import com.orsteg.gesos.androidquizkit.quizParser.SimpleQuizParser
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
    private var mCurrentParser: BaseQuizParser? = null
    private var mCurrentProcess: Deferred<Unit>? = null


    init {
        when (mMethod.mMethod) {
            BuildMethod.Method.RES -> {
                buildFromResource()
            }
            BuildMethod.Method.STREAM -> {
                buildFromStream()
            }
            BuildMethod.Method.STR -> {
                buildFromStringAsync()
            }
            BuildMethod.Method.FILE -> {
                buildFromFile()
            }
            BuildMethod.Method.URL -> {

            }
        }
    }

    private fun buildFromStringAsync() = runBlocking {
        mMethod.mString?.also {
            mCurrentProcess = GlobalScope.async {
                var parser = mBuilder.getQuizParser() ?: mBuilder.getDefaultQuizParser()
                mCurrentParser = parser
                var response: BaseQuizParser.State = BaseQuizParser.State.IDLE
                var hasAppended = false

                loop@ while (!arrayOf(
                        BaseQuizParser.State.FORCE_FINISH, BaseQuizParser.State.PARSE_ERROR,
                        BaseQuizParser.State.VALIDATION_FAILED, BaseQuizParser.State.PARSE_SUCCESS
                    ).contains(response)
                ) {

                    response = if (!hasAppended) {
                        hasAppended = true
                        parser.append(it)
                    } else {
                        parser.finish()
                    }

                    if (response == BaseQuizParser.State.CHANGE_PARSER) {
                        if (mBuilder.getQuizParser() == null) {

                            /* Code to choose new parser
                            val parserInfo = parser.mNewParser

                            parser.cancel()

                            val new = SimpleQuizParser()
                            parser.copy(new)

                            parser = new */
                        }
                    }
                }

                if (arrayOf(
                        BaseQuizParser.State.FORCE_FINISH,
                        BaseQuizParser.State.PARSE_SUCCESS
                    ).contains(response)
                ) {
                    parser.getQuestions()?.apply { questions = this }

                    (context as Activity).runOnUiThread {
                        notifyListener()
                    }

                } else {

                }

                if (mBuilder.getQuizParser() == null) {
                    mBuilder.setQuizParser(parser)
                }
            }

        }
        return@runBlocking
    }

    private fun buildFromStream() {
        mMethod.mStream?.apply {
            buildFromInputStreamAsync(this)
        }
    }

    private fun buildFromResource() {
        mMethod.mRes?.apply {
            buildFromInputStreamAsync(context.resources.openRawResource(this))
        }
    }

    private fun buildFromFile() {
        mMethod.mFile?.also {
            buildFromInputStreamAsync(it.inputStream())
        }
    }


    private fun buildFromInputStreamAsync(quizStream: InputStream) = runBlocking {

        mCurrentProcess = GlobalScope.async {
            val buffer = BufferedInputStream(quizStream, 8192)

            // Holds the number of bytes read for each loop
            var count = 0
            val data = ByteArray(1024)
            var parser = mBuilder.getQuizParser()?: mBuilder.getDefaultQuizParser()
            var response: BaseQuizParser.State = BaseQuizParser.State.IDLE

            try {

                loop@ while (!arrayOf(BaseQuizParser.State.FORCE_FINISH, BaseQuizParser.State.PARSE_ERROR,
                        BaseQuizParser.State.VALIDATION_FAILED, BaseQuizParser.State.PARSE_SUCCESS).contains(response)) {

                    if (count != -1) {
                        count = buffer.read(data)
                    }
                    response = if (count != -1) {
                        parser.append(String(data, 0, count))
                    } else {
                        parser.finish()
                    }

                    if (response == BaseQuizParser.State.CHANGE_PARSER) {
                        if (mBuilder.getQuizParser() == null) {

                            /* Code to choose new parser
                            //val parserInfo = parser.mNewParser

                            parser.cancel()

                            val new = SimpleQuizParser()
                            parser.copy(new)

                            parser = new */
                        }
                    }
                }

                if (arrayOf(
                        BaseQuizParser.State.FORCE_FINISH,
                        BaseQuizParser.State.PARSE_SUCCESS
                    ).contains(response)) {

                    parser.getQuestions()?.apply { questions = this }

                    (context as Activity).runOnUiThread {
                        notifyListener()
                    }

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
        }
        return@runBlocking
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
        if (mCurrentProcess?.isActive == true) mCurrentProcess?.cancel()
        mCurrentParser?.cancel()
    }

    private fun rebuild() {

    }


    override fun getQuiz(config: Quiz.Config, listener: Quiz.OnBuildListener) {
        mQuiz = Quiz1(topic, config)
        mBuildListener = listener

        if (questions.isNotEmpty()) {
            notifyListener()
        }
    }


    class Builder(private val context: Context) {

        private val defQuizParser: SimpleQuizParser = SimpleQuizParser()
        private var mQuizParser: BaseQuizParser? = null

        fun getQuizParser(): BaseQuizParser? = mQuizParser

        fun getDefaultQuizParser(): SimpleQuizParser = defQuizParser

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
            questionIndexes.addAll(run { if (config.randomizeQuestions) generateRandomIndexes()
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