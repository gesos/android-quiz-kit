package com.orsteg.gesos.androidquizkit

interface QuizController {

    fun getCurrentQuestionSet(): List<Question>

    fun nextQuestionSet(): List<Question>

    fun previousQuestionSet(): List<Question>

    fun gotoQuestionSet(set: Int): List<Question>

}