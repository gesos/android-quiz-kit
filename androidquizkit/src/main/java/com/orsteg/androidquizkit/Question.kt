package com.orsteg.androidquizkit

class Question {
    var question: String = ""
    var options: ArrayList<String> = ArrayList()
    var answer: Int = -1
    var selection: Int = -1

    var shouldShowAnswer = false
}