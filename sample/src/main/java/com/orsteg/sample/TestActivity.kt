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


class TestActivity: AppCompatActivity(), Result.EndTestListener {
    private var prefs: SharedPreferences? = null

    lateinit var listener: AdapterView.OnItemClickListener

    lateinit var mQuiz: TimedQuiz

    lateinit var optionAdapter: Options

    lateinit var dialog: LoaderDialog

    var limit: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        title = "GES 300 Test"

        prefs = getSharedPreferences("quiz_limit", Activity.MODE_PRIVATE)

        finish.setOnClickListener {
            finishTest()
        }

        val builder : QuizBuilder = QuizBuilder.Builder(this)
            .build("physics", BuildMethod.fromResource(R.raw.sheet))


        val quizConfig : Quiz.Config = Quiz.Config()
            .randomizeOptions(false)
            .setCount(30)

        builder.getQuiz(quizConfig, object : Quiz.OnBuildListener {
            override fun onFinishBuild(quiz: Quiz) {
                mQuiz = TimedQuiz(quiz, 1000 * 120)
                QuizHistory.restoreState(mQuiz, savedInstanceState)
                startQuiz()
            }
        })



        next.setOnClickListener {
            if (mQuiz.getQuiz().getTotalQuizQuestions() > 0 && mQuiz.getQuiz().selectionState[mQuiz.getQuiz().currentSet] != null) {
                if (limit < mQuiz.getQuiz().currentSet) {
                    checkAnswer()
                } else {
                    nextQuestion()
                }
            }
        }

        previous.setOnClickListener {
            if (mQuiz.getQuiz().currentSet > 0) previousQuestion()
        }


        listener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            mQuiz.getQuiz().selectionState[mQuiz.getQuiz().currentSet] = i

            optionAdapter.notifyDataSetChanged()
        }

        dialog = LoaderDialog(this, true)

        dialog.show()


    }

    fun startQuiz() {
        mQuiz.apply {
            onTimeChangeListener = object : TimedQuiz.OnTimeChangeListener {
                override fun onTimerTick(passedTimeInMillis: Long) {
                    // update time TextView
                }

                override fun onTimerFinish() {
                    finishTest()
                }
            }

            val q = getCurrentQuestionSet()[0]

            optionAdapter = Options(this@TestActivity)

            options.adapter = optionAdapter

            nextQuestion(q)

            dialog.dismiss()

            start()
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

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

        val q = mQuiz.previousQuestionSet()[0]

        index.text = "(" + (mQuiz.getQuiz().currentSet + 1) + ")  of  (${mQuiz.getQuiz().getTotalQuizQuestions()})"


        question.text = q.question
        optionAdapter.setQuestion(q)

    }

    fun nextQuestion(qs: Question? = null) {

        if ((mQuiz.getQuiz().currentSet < mQuiz.getQuiz().getTotalQuizQuestions() - 1) || qs != null) {

            val q = qs?:mQuiz.nextQuestionSet()[0]

            if (mQuiz.getQuiz().currentSet > limit) {
                options.onItemClickListener = listener
                next.text = "Continue"
            } else {
                options.onItemClickListener = null
                next.text = "Next"
            }

            index.text = "(" + (mQuiz.getQuiz().currentSet + 1) + ")  of  (${mQuiz.getQuiz().getTotalQuizQuestions()})"


            question.text = q.question
            optionAdapter.setQuestion(q)

        } else if (mQuiz.getQuiz().getTotalQuizQuestions() > 0) {
            finishTest()
        }
    }

    fun finishTest() {

        mQuiz.cancel()

        val correctCount = QuizHistory.Stats.getNmberOfCorrectAnswers(mQuiz.getQuiz())
        val questionCount = mQuiz.getQuiz().getTotalQuizQuestions()

        val res = Result(this, "$correctCount/$questionCount")
        res.show()
    }

    override fun end() {
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
                if (i == mQuiz.getQuiz().selectionState[q.key]) {
                    view1.state.setImageResource(android.R.drawable.checkbox_on_background)
                }
            } else {
                if (mQuiz.getQuiz().selectionState[q.key] == i) {
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