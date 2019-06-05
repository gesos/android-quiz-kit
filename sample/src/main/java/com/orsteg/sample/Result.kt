package com.orsteg.sample

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import kotlinx.android.synthetic.main.result_layout.*


class Result(var con: Context, private val scoreS: String) : Dialog(con) {
    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.result_layout)

        score.text = scoreS

        ok.setOnClickListener {

            dismiss()
            (con as EndTestListener).end()
        }
    }


    interface EndTestListener {
        fun end()
    }
}