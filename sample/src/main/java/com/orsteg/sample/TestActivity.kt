package com.orsteg.sample

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.os.Bundle
import android.widget.AdapterView
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import com.orsteg.gesos.androidquizkit.builder.BuildMethod
import com.orsteg.gesos.androidquizkit.builder.QuizBuilder
import com.orsteg.gesos.androidquizkit.components.QuizHistory
import com.orsteg.gesos.androidquizkit.components.QuizTimer
import com.orsteg.gesos.androidquizkit.quiz.Question
import com.orsteg.gesos.androidquizkit.quiz.Quiz
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.android.synthetic.main.option_item.view.*
import java.text.SimpleDateFormat
import java.util.*


class TestActivity: AppCompatActivity(), Result.EndTestListener, LoaderDialog.CancelBuildListener {

    private var prefs: SharedPreferences? = null

    lateinit var listener: AdapterView.OnItemClickListener

    var mQuizTimer: QuizTimer? = null

    lateinit var optionAdapter: Options

    lateinit var dialog: LoaderDialog

    lateinit var builder: QuizBuilder

    var result: Result? = null

    var limit: Int = -1

    val formatter = SimpleDateFormat("HH : mm : ss")

    val date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MINUTE, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        title = "GES 300 Test"

        prefs = getSharedPreferences("quiz_limit", Activity.MODE_PRIVATE)

        finish.setOnClickListener {
            finishTest()
        }

        builder = QuizBuilder.Builder(this)
            .build("GES_300", BuildMethod.fromResource(R.raw.sheet))


        val quizConfig : Quiz.Config = Quiz.Config()
            .randomizeOptions(false)
            .setCount(30)

        builder.getQuiz(quizConfig, object : Quiz.OnBuildListener {
            override fun onFinishBuild(quiz: Quiz) {
                mQuizTimer = QuizTimer(quiz, 1000 * 10)
                QuizHistory.restoreState(mQuizTimer!!, savedInstanceState)
                if (savedInstanceState != null) {
                    limit = savedInstanceState.getInt("limit")
                }
                startQuiz()
            }
        })


        next.setOnClickListener {
            if (mQuizTimer!!.getQuestionCount() > 0 && mQuizTimer!!.getSelection() != null) {
                if (limit < mQuizTimer!!.getQuiz().currentIndex) {
                    checkAnswer()
                } else {
                    nextQuestion()
                }
            }
        }

        previous.setOnClickListener {
            if (mQuizTimer!!.getQuiz().currentIndex > 0) previousQuestion()
        }


        listener = AdapterView.OnItemClickListener { _, _, i, _ ->

            mQuizTimer!!.setSelection(i)

            optionAdapter.notifyDataSetChanged()
        }

        dialog = LoaderDialog(this, true)

        dialog.show()


    }

    fun startQuiz() {
        mQuizTimer?.apply {
            onTimeChangeListener = object : QuizTimer.OnTimeChangeListener {
                override fun onTimerTick(timeLeft: Long) {
                    // update time TextView
                    val time = (date.clone() as Calendar).apply { timeInMillis += timeLeft }

                    timer.text = formatter.format(time.time)
                }

                override fun onTimerFinish() {
                    timer.text = formatter.format(date.time)
                    finishTest()
                }
            }

            val q = getCurrentQuestion()

            optionAdapter = Options(this@TestActivity)

            options.adapter = optionAdapter

            nextQuestion(q)

            dialog.dismiss()

            start()
        }
    }

    override fun onPause() {
        super.onPause()
        mQuizTimer?.pause()
        if (dialog.isShowing) dialog.dismiss()
        if (result?.isShowing == true) result?.dismiss()
    }

    override fun onResume() {
        super.onResume()
        mQuizTimer?.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        QuizHistory.saveToBundle(mQuizTimer, outState)
        outState.putInt("limit", limit)

    }

    fun checkAnswer() {
        next.text = "Next"
        options.onItemClickListener = null
        optionAdapter.showAnswer()

        limit++

    }

    fun previousQuestion() {


        options.onItemClickListener = null
        next.text = "Next"

        val q = mQuizTimer!!.previousQuestion()

        index.text = "(" + (mQuizTimer!!.getCurrentQuestionIndex() + 1) + ")  of  (${mQuizTimer!!.getQuestionCount()})"


        question.text = q.statement
        optionAdapter.setQuestion(q)

    }

    fun nextQuestion(qs: Question? = null) {

        if ((mQuizTimer!!.getCurrentQuestionIndex() < mQuizTimer!!.getQuestionCount() - 1) || qs != null) {

            val q = qs?:mQuizTimer!!.nextQuestion()

            if (mQuizTimer!!.getCurrentQuestionIndex() > limit) {
                options.onItemClickListener = listener
                next.text = "Continue"
            } else {
                options.onItemClickListener = null
                next.text = "Next"
            }

            index.text = "(" + (mQuizTimer!!.getCurrentQuestionIndex() + 1) + ")  of  (${mQuizTimer!!.getQuestionCount()})"


            question.text = q.statement
            optionAdapter.setQuestion(q)

        } else if (mQuizTimer!!.getQuestionCount() > 0) {
            finishTest()
        }
    }

    fun finishTest() {

        mQuizTimer!!.finish()

        val correctCount = QuizHistory.Stats.getNumberOfCorrectAnswers(mQuizTimer!!.getQuiz())
        val questionCount = mQuizTimer!!.getQuestionCount()

        result = Result(this, "$correctCount/$questionCount")
        result?.show()
        QuizHistory.getInstance(this).saveToHistory(mQuizTimer!!)
    }

    override fun end() {
        finish()
    }

    override fun cancel() {
        builder.cancel()
        finish()
    }


    inner class Options(var context: Context) : BaseAdapter() {

        var indexes = arrayOf("(A) ", "(B) ", "(C) ", "(D) ")

        var q: Question = Question()

        fun setQuestion(q: Question) {
            this.q = q
            notifyDataSetChanged()
        }

        fun showAnswer() {
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return q.options.size
        }

        override fun getItem(i: Int): String {
            return q.options[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
            var view1 = view

            view1 = LayoutInflater.from(context).inflate(R.layout.option_item, viewGroup, false)

            view1.value.text = indexes[i] + getItem(i)

            if (q.key > limit) {
                if (i == mQuizTimer!!.getSelection()) {
                    view1.state.setImageResource(android.R.drawable.checkbox_on_background)
                }
            } else {
                if (mQuizTimer!!.getSelection() == i) {
                    view1.state.setImageResource(R.drawable.ic_close_black_24dp)
                    view1.setBackgroundColor(context.resources.getColor(R.color.myRed))
                }
                if (q.answer == i) {
                    view1.state.setImageResource(R.drawable.ic_check_black_24dp)
                    view1.setBackgroundColor(context.resources.getColor(R.color.myGreen))
                }
            }

            return view1
        }
    }

}