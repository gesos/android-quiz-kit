package com.orsteg.gesos.androidquizkit.quiz

interface QuizController {

    fun getQuestionCount(): Int

    fun getCurrentQuestionIndex(): Int

    fun getCurrentQuestion(): Question

    fun nextQuestion(): Question

    fun previousQuestion(): Question

    fun gotoQuestion(index: Int): Question

    fun setSelection(index: Int, selection: Int?)

    fun setSelection(selection: Int?)

    fun getSelection(index: Int): Int?

    fun getSelection(): Int?

    fun getResult(index: Int): Int

    fun getResult(): Int
}