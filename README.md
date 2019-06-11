# Android Quiz Kit
Specialized development kit for building standard quiz apps. Built with `kotlin` and lots of :coffee:

## Introduction
> Quote

There's a difference between setting quiz questions and programming quiz questions. The aim of this library is to bridge the gap between setting actual quiz questions and building a quiz app. Questions can be set by simply creating a text file containing the questions written as though it was supposed to be a hand written quiz :smile:. The file is then converted by the API into a complete `Quiz` object used by the app. The app can thus be built in a very dynamic way by allowing the quiz to be created either from a file in phone storage, an input stream, a String, or from the internet.

The API is built to be both simple to use and highly customizable for developers. Inspired by flutter’s Widgets, a component based design is used allowing new quiz components to be designed and included to the app to change specific behaviors without interfering with the base implementations of the API. **P.S: For lovers of flutter, a Dart version of the API is in production and will soon be made available**.

## Table of Contents
- [Android Quiz Kit](#android-quiz-kit)
  - [Introduction](#introduction)
  - [Table of Contents](#table-of-contents)
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
      - [Other Quiz Methods](#other-quiz-methods)
  - [The Question Object](#the-question-object)
      - [Question Sample usage](#question-sample-usage)
    - [Answering a Question](#answering-a-question)
  - [Quiz Components](#quiz-components)
    - [Saving History](#saving-history)
    - [Adding a Timer](#adding-a-timer)
    - [Getting Quiz Stats](#getting-quiz-stats)
  - [Contributing](#contributing)
  - [License](#license)
  - [Contact](#contact)

## How to Use

### Including Dependencies
In your app level gradle file add the following dependency

```groovy

```
### Setting Questions
For the simple case of multiple choice questions (Interpreted by the `SimpleQuizParser` class. For other question formats, see [Setting a QuizParser]()), create a file containing the questions written in the following format.
1. Add the `<!QUIZ>` header at the beginning of the file. This is used to verify that the contents of the file are quiz questions. **To disable this feature and remove the header see** [Removing quiz verification header]().
2. New line characters (paragraphs) are used to determine the begining and end of a question or option. Place each question and individual option on a new line.
3. For readability, place identifiers like `1.`, `2.`, `3.`,... at the begining of each question and `a.`, `b.`, `c.` ... at the begining of each option. The identifiers are ignored when reading the file, as such they can actually be ommitted, but for readability they are recommended to be placed. **If you want to show identifiers while displaying the questions in the app, you would have to append it manually while updating the UI see** [Displaying questions](). 
4. Each question should have the same number of options. The default number of options is 4(four). **If the number of options for each questions is less or greater than 4 see** [Specifying number of options]().
5. Place asterisks `*` at the begining of the option with the correct answer.**To specify a different answer marker see** [Specifying answer marker]().

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

5. ~~Building from a url~~ not yet implemented
```kotlin
val method : Builmethod = BuildMethod.fromUrl(quizUrl)
```

#### Creating the QuizBuilder
```kotlin
val builder : QuizBuilder = QuizBuilder.Builder(context).build("topic", method)
```

#### Getting the Quiz
The `Quiz` object is built asyncronously. call `getQuiz()` on the QuizBuilder to retreive the quiz once it is done building. `getQuiz()` takes two arguments; a `Quiz.Config` object for setting quiz configurations and a `Quiz.OnBuildListener` to retreive the Quiz.

```kotlin
builder.getQuiz(Quiz.Config(), object : Quiz.OnBuildListener {
            override fun onFinishBuild(quiz: Quiz) {

            }
        })
```

#### Setting Quiz Config
The `Quiz.Config` object holds basic configurations for the quiz. Its public properties and their default values include the following:
```kotlin
var randomizeOptions: Boolean = false // Randomises the options for a question
var randomizeQuestions: Boolean = true // Randomises the questions
var questionCount: Int = -1 // Sets the number of questions to be retreived for the quiz. A value of -1 retreives all the questions
var groupSize: Int = 1 // Sets the number of questions that would be displayed at once to the user. If value is -1 a call to getQuestionGroup() from the Quiz would return all the questions retreived by the Quiz at once.
```


## The Quiz API
The Quiz returns questions in groups. A Group represents the questions that would be displayed at a time to the user (**Default group size is one**). A list of questions (`List<Question>`) is returned from a call to any `QuestionGroup()` method. The quiz tracks which questions are currently being displayed by using the `currentGroup` property. The following are some public methods provided by the Quiz object.

#### The QuizController
```kotlin
fun getCurrentQuestionGroup(): List<Question> // returns the questions at currentGroup

fun nextQuestionGroup(): List<Question> // increments currentGroup by one and returns the questions

fun previousQuestionGroup(): List<Question> // decreases currentGroup by one and returns the questions

fun gotoQuestionGroup(group: Int): List<Question> // sets the currentGroup and returns the questions
```

#### Other Quiz Methods
```kotlin
fun getTotalQuizQuestions(): Int // returns the total number of questions on the quiz


fun getQuestionRange(startIndex: Int, stopIndex: Int) : List<Question> // gets questions at within a range

fun getQuestions(indexes: List<Int>): List<Question> // gets questions at a list of indexes

fun getQuestion(index: Int): Question // gets a question at a particular index


fun getGroupCount(): Int // gets the total group count

fun getGroupSize(): Int // gets the maximum size of each group

fun getLastGroupSize(): Int // gets the size of the last group

fun getGroupForQuestionIndex(index: Int): Int // returns the group number for a particular question

```


## The Question Object
The `Question` object contains the information for a particular question. It includes the question statement, the options, the correct answer and an identifying key.

#### Question Sample usage
```kotlin
// retreiving the question for a quiz groupSize of 1
val question: Question = quiz.getCurrentQuestionGroup()[0] 

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
The Quiz object is designed to handle the selection made for a particular question. To answer a question first display the options on the UI. When an option is chosen get the index corresponding to that option.
```kotlin

```


## Quiz Components
Components are designed to perform additional tasks like creating a history, implementing a timer, or some other specific functionalities. The following componenets are made available in the library
### Saving History
```kotlin

```

### Adding a Timer
```kotlin

```

### Getting Quiz Stats
```kotlin

```

## Contributing

## License
This project is licensed under the Apache License Version 2.0, January 2004. Read the [license](/LICENSE) statement for more information.

## Contact
- Support: [orsteg.apps@gmail.com](mailto:orsteg.apps@gmail.com)
- Developer: [goodhopeordu@yahoo.com](mailto:goodhopeordu@yahoo.com)
- Website: [https://orsteg.com](https://orsteg.com)

