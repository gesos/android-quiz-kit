package com.orsteg.sample

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window

/**
 * Created by goodhope on 4/28/18.
 */
class LoaderDialog(private val cont: Context, private val cancelable: Boolean = false) : Dialog(cont) {

    init {
        setCancelable(false)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.loader_layout)

        if (cancelable) setCancelable {
            (cont as Result.EndTestListener).end()
        }
    }

    fun setCancelable(onAbort: () -> Unit) {
        (findViewById<View>(R.id.abort)).visibility = View.VISIBLE
        (findViewById<View>(R.id.abort)).setOnClickListener {
            dismiss()
            onAbort()
        }
    }

}
