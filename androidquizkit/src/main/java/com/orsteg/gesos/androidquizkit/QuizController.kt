package com.orsteg.gesos.androidquizkit

interface QuizController {

    fun getCurrentQuestionGroup(): List<Question>

    fun nextQuestionGroup(): List<Question>

    fun previousQuestionGroup(): List<Question>

    fun gotoQuestionGroup(group: Int): List<Question>

    fun setSelection(index: Int)

    fun getSelection()
}