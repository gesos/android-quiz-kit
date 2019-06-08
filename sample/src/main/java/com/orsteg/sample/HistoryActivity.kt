package com.orsteg.sample

import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import com.orsteg.gesos.androidquizkit.QuizHistory
import kotlinx.android.synthetic.main.activity_history.*


class HistoryActivity: AppCompatActivity() {

    lateinit var h: History
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        title = "Test History"

        val dialog = LoaderDialog(this, true)

        dialog.show()

        Thread(Runnable {

            val s = ArrayList<String>()

            for (stat in QuizHistory.getInstance(this).getAllStats()) {
                s.add("${stat.correctCount}/${stat.questionCount}")
            }

            h = History(this, s)

            runOnUiThread {
                dialog.dismiss()
                history.emptyView = findViewById<View>(R.id.empty)
                history.adapter = h
            }
        }).start()

    }

    fun end() {
        finish()
    }

    inner class History(var context: Context, var history: ArrayList<String>) : BaseAdapter() {

        override fun getCount(): Int {
            return history.size
        }

        override fun getItem(i: Int): String {
            return history[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
            var view = view

            val holder: Holder
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.history_item, viewGroup, false)

                holder = Holder()
                holder.value = view!!.findViewById(R.id.txt)

                view.tag = holder
            } else {
                holder = view.tag as Holder
            }

            holder.value!!.text = "(" + (i + 1) + ")   " + getItem(i)

            return view
        }

        inner class Holder {
            internal var value: TextView? = null
        }
    }

}