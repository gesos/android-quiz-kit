package com.orsteg.gesos.androidquizkit

import kotlin.collections.ArrayList

open class Question {
    var key: Int = -1
    var question: String = ""
    var options: ArrayList<String> = ArrayList()
    var answer: Int = -1
    var hasInit: Boolean = false

    open fun init(config: Quiz.Config, initSeed: Int) {
        if (config.mRandomizeOptions) {

            val seed = (initSeed + key + 5214) * question.length + 3142
            val ids = (seed.toString() + "3142").toCharArray().map { (it.toInt() % 4) }
            val ar = ArrayList<Int>()
            for (i in ids) {
                if (!ar.contains(i)) ar.add(i)
            }

            val nOptions = (0 until options.size).map { "" }.toMutableList()

            for (o in 0..3) {
                nOptions[ar[o]] = options[o]
            }

            answer = ar[answer]
            options = ArrayList<String>().apply {
                addAll(nOptions)
            }

        }
    }

}