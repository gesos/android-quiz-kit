package com.orsteg.androidquizkit

open class Question {
    var question: String = ""
    var options: ArrayList<String> = ArrayList()
    var answer: Int = -1

    open fun run(config: Quiz.Config):Int {

        if (config.mRandomizeOptions) {
            val i = (1..4).toMutableList().apply {
                shuffle()
            }.toList()

            return i[0]*1000 + i[1]*100 + i[2]*10 + i[3]
        }

        return 0
    }

    open fun restore(data: Int) {

    }

    open fun setSelection() {

    }
}