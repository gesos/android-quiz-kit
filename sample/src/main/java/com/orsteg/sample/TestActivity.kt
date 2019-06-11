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
import com.orsteg.gesos.androidquizkit.*
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.android.synthetic.main.option_item.view.*
import java.text.SimpleDateFormat
import java.util.*


class TestActivity: AppCompatActivity(), Result.EndTestListener, LoaderDialog.CancelBuildListener {

    private var prefs: SharedPreferences? = null

    lateinit var listener: AdapterView.OnItemClickListener

    var mQuiz: TimedQuiz? = null

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
            .build("physics", BuildMethod.fromResource(R.raw.sheet))


        val quizConfig : Quiz.Config = Quiz.Config()
            .randomizeOptions(false)
            .setCount(30)

        builder.getQuiz(quizConfig, object : Quiz.OnBuildListener {
            override fun onFinishBuild(quiz: Quiz) {
                mQuiz = TimedQuiz(quiz, 1000 * 10)
                QuizHistory.restoreState(mQuiz!!, savedInstanceState)
                if (savedInstanceState != null) {
                    limit = savedInstanceState.getInt("limit")
                }
                startQuiz()
            }
        })



        next.setOnClickListener {
            if (mQuiz!!.getQuiz().getTotalQuizQuestions() > 0 && mQuiz!!.getQuiz().selectionState[mQuiz!!.getQuiz().currentGroup] != null) {
                if (limit < mQuiz!!.getQuiz().currentGroup) {
                    checkAnswer()
                } else {
                    nextQuestion()
                }
            }
        }

        previous.setOnClickListener {
            if (mQuiz!!.getQuiz().currentGroup > 0) previousQuestion()
        }


        listener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            mQuiz!!.getQuiz().selectionState[mQuiz!!.getQuiz().currentGroup] = i

            optionAdapter.notifyDataSetChanged()
        }

        dialog = LoaderDialog(this, true)

        dialog.show()


    }

    fun startQuiz() {
        mQuiz?.apply {
            onTimeChangeListener = object : TimedQuiz.OnTimeChangeListener {
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

            val q = getCurrentQuestionGroup()[0]

            optionAdapter = Options(this@TestActivity)

            options.adapter = optionAdapter

            nextQuestion(q)

            dialog.dismiss()

            start()
        }
    }

    override fun onPause() {
        super.onPause()
        mQuiz?.pause()
        if (dialog.isShowing) dialog.dismiss()
        if (result?.isShowing == true) result?.dismiss()
    }

    override fun onResume() {
        super.onResume()
        mQuiz?.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        QuizHistory.saveToBundle(mQuiz, outState)
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

        val q = mQuiz!!.previousQuestionGroup()[0]

        index.text = "(" + (mQuiz!!.getQuiz().currentGroup + 1) + ")  of  (${mQuiz!!.getQuiz().getTotalQuizQuestions()})"


        question.text = q.statement
        optionAdapter.setQuestion(q)

    }

    fun nextQuestion(qs: Question? = null) {

        if ((mQuiz!!.getQuiz().currentGroup < mQuiz!!.getQuiz().getTotalQuizQuestions() - 1) || qs != null) {

            val q = qs?:mQuiz!!.nextQuestionGroup()[0]

            if (mQuiz!!.getQuiz().currentGroup > limit) {
                options.onItemClickListener = listener
                next.text = "Continue"
            } else {
                options.onItemClickListener = null
                next.text = "Next"
            }

            index.text = "(" + (mQuiz!!.getQuiz().currentGroup + 1) + ")  of  (${mQuiz!!.getQuiz().getTotalQuizQuestions()})"


            question.text = q.statement
            optionAdapter.setQuestion(q)

        } else if (mQuiz!!.getQuiz().getTotalQuizQuestions() > 0) {
            finishTest()
        }
    }

    fun finishTest() {

        mQuiz!!.finish()

        val correctCount = QuizHistory.Stats.getNumberOfCorrectAnswers(mQuiz!!.getQuiz())
        val questionCount = mQuiz!!.getQuiz().getTotalQuizQuestions()

        result = Result(this, "$correctCount/$questionCount")
        result?.show()
        QuizHistory.getInstance(this).saveToHistory(mQuiz!!)
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
                if (i == mQuiz!!.getQuiz().selectionState[q.key]) {
                    view1.state.setImageResource(android.R.drawable.checkbox_on_background)
                }
            } else {
                if (mQuiz!!.getQuiz().selectionState[q.key] == i) {
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