package com.orsteg.androidquizkit

abstract class BaseQuizParser {

    abstract val headerByteSize: Int

    var mNewParser: String = ""
    var mState: State = State.IDLE
    var mBuffer: StringBuffer = StringBuffer()
    var isParseComplete: Boolean = false
    var hasFinished: Boolean = false
    var mPointer: Int = 0

    fun append(data: ByteArray): State {

        if (arrayOf(State.VALIDATION_FAILED, State.PARSE_ERROR,
                State.FORCE_FINISH).contains(mState)) return mState

        mBuffer.append(data)

        if (mBuffer.length >= headerByteSize && mState == State.IDLE) {

            mState = State.VALIDATION_START

            Thread{
                if (!validate()) {
                    mState = State.VALIDATION_FAILED
                } else {
                    mState = State.VALIDATION_SUCCESS

                    while (!arrayOf(State.PARSE_ERROR, State.CANCELLED, State.PARSE_SUCCESS,
                            State.FORCE_FINISH).contains(mState)) {

                        mPointer = parse(mPointer)

                        if (mState == State.END_OT_DATA && mPointer == mBuffer.length -1) {
                            mState = State.PARSE_SUCCESS
                        }
                    }
                }
                isParseComplete = true

            }.start()
        }

        return mState
    }

    fun finish(): State {

        if (!hasFinished) {
            if (!arrayOf(State.FORCE_FINISH, State.VALIDATION_FAILED, State.PARSE_ERROR).contains(mState)) {
                mState = State.END_OT_DATA
            }

            while (!isParseComplete);

            if (arrayOf(State.PARSE_SUCCESS, State.FORCE_FINISH).contains(mState)) {
                onFinish()
            } else {
                onFailed()
            }

            hasFinished = true
        }
        return mState
    }


    fun cancel() {
        mState = State.CANCELLED
        onCancel()
    }

    fun setState(state: State) {
        mState = state
    }

    fun getState(): State = mState

    fun copy(parser: BaseQuizParser) {
        parser.mBuffer = mBuffer
    }

    open fun reset() {
        mNewParser = ""
        mPointer = 0
        mBuffer = StringBuffer()
        mState = State.IDLE
        isParseComplete = false
        hasFinished = false
    }

    open fun onFinish() {

    }

    open fun onFailed() {

    }

    open fun onCancel() {

    }


    abstract fun validate(): Boolean

    abstract fun parse(pointer: Int): Int

    abstract fun getQuestions(): Array<Question>?


    enum class State {
        IDLE, CANCELLED, END_OT_DATA, PARSE_SUCCESS, PARSE_ERROR, FORCE_FINISH, CHANGE_PARSER,
        VALIDATION_START, VALIDATION_FAILED, VALIDATION_SUCCESS
    }

}