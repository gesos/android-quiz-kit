package com.orsteg.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.orsteg.gesos.androidquizkit.components.QuizHistory
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        start.setOnClickListener {
            startTest()
        }

        history.setOnClickListener {
            viewHistory()
        }

    }

    override fun onStart() {
        super.onStart()

        score.text = QuizHistory.getInstance(this).getAllStats().run {
            if (isNotEmpty()) {
                "${first().correctCount}/${first().questionCount}"
            } else {
                "0/0"
            }
        }

    }

    private fun startTest() {
        val intent = Intent(this, TestActivity::class.java)
        startActivity(intent)
    }

    private fun viewHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
}
