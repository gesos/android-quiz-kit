# Android Quiz Kit
Specialized development kit for building standard quiz apps. Built with `kotlin` and lots of :coffee:

## Introduction
> ## Making questions for a quiz app should be one of the least daunting tasks, but most times it isn't!

There's a difference between setting quiz questions and programming quiz questions. The aim of this library is to bridge the gap between setting actual quiz questions and building a quiz app. Questions can be set by simply creating a text file containing the questions written as though it was supposed to be a hand written quiz :smile:. The file is then converted by the API into a complete `Quiz` object used by the app. The app can thus be built in a very dynamic way by allowing the quiz to be created either from a file in phone storage, an input stream, a String, or from the internet.

The API is built to be both simple to use and highly customizable for developers. Inspired by flutter’s Widgets, a component based design is used allowing new quiz components to be designed and included to the app to change specific behaviors without interfering with the base implementations of the API. **P.S: For lovers of flutter, a Dart version of the API is in production and will soon be made available**.

## Demo
View demo [app](https://github.com/gesos/ges-300-quiz-app) and source [code](/sample) for usage.

This library is used in building the [Uniport GES 300 Quiz App](https://github.com/gesos/ges-300-quiz-app). Built something great with this library? feel free to let us know :+1:.

## Table of Contents
  - [Introduction](#introduction)
  - [Demo](#demo)
  - [How to Use](#how-to-use)
    - [Including Dependencies](#including-dependencies)
    - [Setting Questions](#setting-questions)
      - [Sample File](#sample-file)
    - [Building the Quiz](#building-the-quiz)
      - [Build Methods](#build-methods)
      - [Creating the QuizBuilder](#creating-the-quizbuilder)
      - [Getting the Quiz](#getting-the-quiz)
      - [Setting Quiz Config](#setting-quiz-config)
  - [The Quiz API](#the-quiz-api)
      - [The QuizController](#the-quizcontroller)
  - [The Question Object](#the-question-object)
      - [Question Sample usage](#question-sample-usage)
    - [Answering a Question](#answering-a-question)
  - [Quiz Components](#quiz-components)
    - [Using History](#using-history)
      - [Saving History](#saving-history)
      - [Saving Instace State](#saving-instance-state)
      - [Restoring History](#restoring-history)
      - [The History Component](#the-history-component)
    - [Getting Quiz Stats](#getting-quiz-stats)
    - [Adding a Timer](#adding-a-timer)
      - [Listening for time](#listening-for-time)
      - [Pausing and Resuming Timer](#pausing-and-resuming-timer)
      - [Getting Timed Quiz Stats](#getting-timed-quiz-stats)
  - [Contributing](#contributing)
  - [License](#license)
  - [Contact](#contact)

## How to Use

### Including Dependencies
The project can be pulled from jitpack using the required release.

In your root/project level gradle file add the following dependency

```groovy
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
}
```

In your app level gradle file add the following dependency

```groovy
dependencies {
  implementation 'com.github.gesos:android-quiz-kit:v0.0.1-beta'
}
```
### Setting Questions
For the simple case of multiple choice questions (Interpreted by the `SimpleQuizParser` class. For other question formats, see [Setting a QuizParser](/docs/using-the-quiz-builder.md#setting-a-quizparser)), create a file containing the questions written in the following format.
1. Add the `<!QUIZ>` header at the beginning of the file. This is used to verify that the contents of the file are quiz questions. **To disable this feature and remove the header see** [Disabling verification](/docs/using-the-quiz-parser.md#disabling-verification)).
2. New line characters (paragraphs) are used to determine the begining and end of a question or option. Place each question and individual option on a new line.
3. For readability, place identifiers like `1.`, `2.`, `3.`,... at the begining of each question and `a.`, `b.`, `c.` ... at the begining of each option. The identifiers are ignored when reading the file, as such they can actually be ommitted, but for readability they are recommended to be placed. **If you want to show identifiers while displaying the questions in the app, you would have to append it manually while updating the UI see** [Displaying questions](/docs/using-the-question-object.md#displaying-questions)). 
4. Each question should have the same number of options. The default number of options is 4(four). **If the number of options for each questions is less or greater than 4 see** [Specifying number of options](/docs/using-the-quiz-parser.md#specifying-number-of-options)).
5. Place asterisks `*` at the begining of the option with the correct answer.**To specify a different answer marker see** [Specifying answer marker](/docs/using-the-quiz-parser.md#specifying-answer-marker)).

#### Sample File

```
<!QUIZ>

1. What programming language was this library written with?
a. javascript
b. python
*c. kotlin
d. c++ 

2. What is the aim of this library?
a. to drink coffee
*b. to bridge the gap between setting actual quiz questions and building a quiz app
c. to have fun
d. none of the above 

```

### Building the Quiz
To get the quiz you first need to create a `QuizBuilder`, assigning it a `BuildMethod` and a topic

#### Build Methods
There are five BuildMethods provided by the API

1. Building from string
```kotlin
val method : Builmethod = BuildMethod.fromString(quizString)
```

2. Building from InputStream
```kotlin
val method : Builmethod = BuildMethod.fromInputStream(quizStream)
```

3. Building from Android Raw Resources
```kotlin
val method : Builmethod = BuildMethod.fromResource(quizRes)
```

4. Building from a File
```kotlin
val method : Builmethod = BuildMethod.fromFile(quizFile)
```

5. ~~Building from a url~~ Not yet implemented. Watch out for next release.
```kotlin
val method : Builmethod = BuildMethod.fromUrl(quizUrl)
```

#### Creating the QuizBuilder
```kotlin
val builder : QuizBuilder = QuizBuilder.Builder(context).build("topic", method)
```

#### Getting the Quiz
The `Quiz` object is built asyncronously (any form of progress indicator can be used during this short time). Call `getQuiz()` on the QuizBuilder to retreive the quiz once it is done building. `getQuiz()` takes two arguments; a `Quiz.Config` object for setting quiz configurations and a `Quiz.OnBuildListener` to retreive the Quiz.

```kotlin
builder.getQuiz(Quiz.Config(), object : Quiz.OnBuildListener {
        override fun onFinishBuild(quiz: Quiz) {
            // Initialise the quiz UI here, and cancel any progress indicators
        }
    })
```

#### Setting Quiz Config
The `Quiz.Config` object holds basic configurations for the quiz. Its public properties and their default values include the following:
```kotlin
// Randomizes the options for a question
var randomizeOptions: Boolean = false

// Randomizes the questions
var randomizeQuestions: Boolean = true 

// Sets the number of questions to be retreived for the quiz. A value of -1 retreives all the questions
var questionCount: Int = -1 
```


## The Quiz API
A `Questions` object is returned from a call to any `getQuestion()` method. The quiz tracks which question is currently being displayed by using the `currentIndex` property. The `Quiz` class implements the [`QuizController`](/androidquizkit/src/main/java/com/orsteg/gesos/androidquizkit/quiz/QuizController.kt) interface. The following are some public methods provided by the `Quiz` object.

#### The QuizController
The interface below shows the most useful quiz controller methods.
```kotlin
// returns the total number of questions retreived for the quiz
fun getQuestionCount(): Int

// gets the current question tracking index of the quiz
fun getCurrentQuestionIndex(): Int

// returns the question at current index
fun getCurrentQuestion(): Question

// increments the index by 1 and retreives the question
fun nextQuestion(): Question

// decreases the index by 1 and retreives the question
fun previousQuestion(): Question

// stores the selected answer for the current question
fun setSelection(selection: Int?)

// gets the selected answer for the current question
fun getSelection(): Int?

// gets the result of the selection for the current question. 1 for correct, 0 for wrong, -1 for unselected
fun getResult(): Int
```

## The Question Object
The `Question` object contains the information for a particular question. It includes the question statement, the options, the correct answer and an identifying key.

#### Question Sample usage
```kotlin
// retreiving the question
val question: Question = quiz.getCurrentQuestion() 

// getting the question's properties
val statement: String = question.statement
val options: List<String> = question.options
val answerIndex: Int = question.answer

// setting up your UI
statementTextView.text = statement
optionsList.adapter = createAdapter(options) 

val correctOption: String = options[answerIndex]
```

### Answering a Question
The Quiz object is designed to handle the selection made for a particular question. To answer a question first display the options on the UI. Listen for user selection. Oce selected get, get the index corresponding to that option and make a call to `setSelection()`.
```kotlin
// sample for using an AdapterView.OnItemClickListener for a ListView to listen for user selection
optionsList.onItemClickListener = AdapterView.OnItemClickListener { _, _, optionIndex, _ ->
        quiz.setSelection(optionIndex)
    }
```


## Quiz Components
Components are designed to perform additional tasks like creating a history, implementing a timer, or some other specific functionalities. The following componenets are made available in the library

### Using History
Saving history is implemented using the `QuizHistory` class. Being able to persist user state and data is an important feature for any app. Using this componenet, a quiz can be paused and resumed from where the user last stopped (like a Quiz Game), simply persisting data in a bundle acrross the Activity's Lifecycle or even letting the user review his performance on a previously completed quiz.

#### Saving History
To save a completed or uncompleted quiz to history, get an instance of QuizHistory, and call `saveToHistory()`. Each quiz is associated with a specific `id` or timestamp (a `Long` number)which represents the time the quiz started. This id is used in saving to history and should also be used in retreiving the history.
```kotlin
QuizHistory.getInstance(context).saveToHistory(quiz)

// pass a Boolean with value of true as a second argument to save an uncompleted quiz
QuizHistory.getInstance(context).saveToHistory(quiz, true)
```
#### Saving Instance State
To persist data in a bundle acrross the Activity's Lifecycle, call the following method in the Activity's `onSavedInstanceState()` callback
```kotlin
override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    QuizHistory.saveToBundle(quiz, outState)
}
```
#### Restoring History
Resoring of state is done either from bundle or history. The bundle is first checked for any saved state before checking the history using the unique quiz id supplied to it if any.
```kotlin
QuizHistory.restoreState(quiz, savedInstanceState)

// pass an Long containing the quiz id as a third argument to review a completed quiz
QuizHistory.restoreState(quiz, savedInstanceState, quizId)

// pass a Boolean with value of true as a fourth argument to restore an uncompleted quiz
QuizHistory.restoreState(quiz, savedInstanceState, quizId, true)
```

#### The HistoryComponent
The [`HistoryComponenet`](/androidquizkit/src/main/java/com/orsteg/gesos/androidquizkit/components/HistoryComponent.kt) is an interface that can be extended for the purpose of saving history with the `QuizHistory` class. Classes that extend it needs access to a `Quiz` object. These classes can also be passesed as a parameter where a `Quiz` object is reequired in `QuizHistory` methods.

### Getting Quiz Stats
This is a utility class of the `QuizHistory` class. Stats are a way of displaying user performance on different quiz sessions. To get all Stats for completed quizes use:
```kotlin
// returns a list of stats
val allStats: List<QuizHistory.Stats> = QuizHistory.getInstance(this).getAllStats()

// returns the stats for a particular quizid
val allStats: QuizHistory.Stats = QuizHistory.getInstance(this).getStat(quizId)
```

The following are the information included in the `Stats` object
```kotlin
// The topic assigned to the quiz
var topic: String

// the timestamp of the quiz same as the quiz id
var quizTimestamp: Long

// the index of attempted questions
var answeredIndexes: List<Int>?

// the number of attempted questions
var answeredCount: Int

// the indexes of correct answers 
var correctIndexes: List<Int>?

// the number of correct answere
var correctCount: Int

// the number of questions set for the quiz
var questionCount: Int
```
A possible use case includes showing a recent history of stats, and letting the user review his performance on that session by clicking on a Stat, whose timestamp (quiz id) will then be passed in an intent to another activity. The timestamp is then used to rebuild the finished quiz session from history. see demo source [code](/sample/src/main/java/com/orsteg/sample/HistoryActivity.kt) for example.

### Adding a Timer
To make things a little more interesting and challenging a timer can be included into the quiz. The library includes a `QuizTimer` class for this functionality. Build the timer by passing the Quiz and the total time for the quiz to its public constructor. The `QuizTimer` implements the `HistoryComponent` and `QuizController`(Delegated to the quiz object received) interfaces, and as such can be passed as an argument to `QuizHistory` in place of a `Quiz` Object. Public `QuizController` methods such as `getCurrentQuestion()` can also be called from this class.

```kotlin
// pass in the quiz object and the total time for the quiz
val quizTimer: QuizTimer = QuizTimer(quiz, 10000)

// restore both the quiz history and the timer history
QuizHistory.restoreState(quizTimer, savedInstanceState)

// get question
val question: Question = quizTimer.getCurrentQuestion()

// ...

// Start the timer
quizTimer.start()
```

#### Listening for Time
Listen for changes in time if you want to update a TextView, or end the quiz once its done. Do so by including an `OnTimeChangeListener` to the timer.
```kotlin
quizTimer.onTimeChangeListener = object : QuizTimer.OnTimeChangeListener {
        override fun onTimerTick(timeLeft: Long) {
            // update time TextView
        }

        override fun onTimerFinish() {
            finishQuiz()
        }
    }
```

#### Pausing and Resuming Timer
The timer might need to be suspended, paused, finished or cancelled at diferent points in time, and then started at other points in time.

1. **Suspending the timer**: Here the timer is not entirely stopped. It stops observing the changes in time, but records the value of the last time it observed from the realClockTime of the device. When the timer is then started at a later point, it calculates how much time has passed since it was suspended and removes it from the remaining time. Can be usedin Activity's callback like `onPause()` when the UI mignt not currently be visible to the user.
   
```kotlin
override fun onPause() {
    super.onPause()
    quizTimer.suspend()
}
```

2. **Pausing the timer**: Here the timer is entirely stoped and started from the same point on the next start. Call this when you want to pause an unfinished quiz before saving to history.

```kotlin
fun pauseQuiz() {
    quizTimer.pause()
    // pass a Boolean with value of true as a second argument to save an uncompleted quiz
    QuizHistory.getInstance(context).saveToHistory(quizTimer, true)

    //...

}
```

3. **Finishing the timer**: If the user finishes the quiz before the timer ends, this method should be used to stop the timer.
```kotlin
fun finishQuiz() {
    quizTimer.finish()
    QuizHistory.getInstance(context).saveToHistory(quizTimer)

    //...

}
```

4. **Cancelling the timer**: When the quiz is cancelled (perhaps by the user) this method should be used to properly stop the timer.
```kotlin
fun cancelQuiz() {
    quizTimer.cancel()

    //...

}
```

5. **Resuming the timer**: Whatever the method used in stopping the timer, the same method is used each time in starting it. It can be used to begin the timer when it is first initialized or resume it after is has been paused in the activit's `onResume()` callback **(a null safe check might be necessary before starting the timer in `onResume()` as the variable might not have been initialised on Activity's creation due to its dependency on the `Quiz` object)**.
```kotlin

// nullable quiz timer
var quizTimer: QuizTimer? = null

// Might be called at a later time after onResume() has already been called
fun initializeTimer(quiz: Quiz) {
  val timer = QuizTimer(quiz, 10000)

  //...

  quizTimer = timer
  timer.start()
}

override fun onResume() {
    super.onResume()
    // Null safe check
    quizTimer?.start()
}
```

#### Getting Timed Quiz Stats
The `QuizTimer` also provides an extension of `QuizHistory.Stats` called `TimedStats`. It includes two more stats; the quiz totalTime and the user's finishTime. Get the Timed stats by calling `QuizTimer.getStat()`
```kotlin
val stats: TimedStats? = QuizTimer.getStat(context, quizId)
val finishTime = stats.finishTime
val totalTime = stats.TotalTime
```

## Contributing
Contributions are open for this project. A Contribution and Issues guideline will soon be created for this purpose. Till then feel free to fork, contribute and post issues.

## License
This project is licensed under the Apache License Version 2.0, January 2004. Read the [license](/LICENSE) statement for more information.

## Contact
- Support: [orsteg.apps@gmail.com](mailto:orsteg.apps@gmail.com)
- Developer: [goodhopeordu@yahoo.com](mailto:goodhopeordu@yahoo.com)
- Website: [https://orsteg.com](https://orsteg.com)

