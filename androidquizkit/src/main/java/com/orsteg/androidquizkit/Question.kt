package com.orsteg.androidquizkit

import kotlin.collections.ArrayList

open class Question {
    var question: String = ""
    var options: List<String> = ArrayList()
    var answer: Int = -1
    var hasInit: Boolean = false

    open fun init(config: Quiz.Config, initState: Int?):Int {
        if (config.mRandomizeOptions) {

            val i = initState?.toString()?.toCharArray()?.map { it.toInt() }
                ?: (1..4).toMutableList().apply {
                    shuffle()
                }.toList()

            val nOptions = (0 until options.size).map { "" }.toMutableList()

            for (o in 0..3) {
                nOptions[i[o] - 1] = options[o]
            }

            answer = i[answer]
            options = nOptions

            return i[0]*1000 + i[1]*100 + i[2]*10 + i[3]
        }
        return 0
    }

}