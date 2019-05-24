package com.orsteg.androidquizkit

class Test {

    init {
        val builder : QuizBuilder = QuizBuilder.Builder().
            setAnswerMarker("*").
            setOptionsCount(4).
            build(BuildMethod.fromString(""))


        val quizConfig : Quiz.Config = Quiz.Config().
            randomizeOptions().
            randomizeQuestions().
            setCount(30).
            maxSetSize(5)

        builder.getQuiz(quizConfig, object : Quiz.OnBuildListener {
            override fun onFinishBuild(quiz: Quiz) {
                quiz.getCurrentQuestionSet()
            }
        })
    }
}