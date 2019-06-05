package com.orsteg.gesos.androidquizkit

import android.util.Log

abstract class BaseQuizParser {

    abstract val headerByteSize: Int

    var mNewParser: String = ""
    @Volatile var mState: State = State.IDLE
    var mBuffer: StringBuffer = StringBuffer()
    @Volatile var isParseComplete: Boolean = false
    var hasFinished: Boolean = false
    var mPointer: Int = 0

    fun append(data: ByteArray, size: Int): State {

        if (arrayOf(State.VALIDATION_FAILED, State.PARSE_ERROR,
                State.FORCE_FINISH).contains(mState)) return mState

        mBuffer.append(String(data, 0, size))

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

                        if (mState == State.END_OT_DATA && mPointer == mBuffer.length) {
                            mState = State.PARSE_SUCCESS
                        }
                        Log.d("tg", "return loop")
                    }
                }
                isParseComplete = true
                Log.d("tg", isParseComplete.toString())

            }.start()
        }

        return mState
    }

    fun finish(): State {

        if (!hasFinished) {
            if (!arrayOf(State.FORCE_FINISH, State.VALIDATION_FAILED, State.PARSE_ERROR).contains(mState)) {
                mState = State.END_OT_DATA
            }

            Log.d("tg", "waiting finish")


            while (!isParseComplete);

            Log.d("tg", "finish complete")

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

    abstract fun getQuestions(): List<Question>?


    enum class State {
        IDLE, CANCELLED, END_OT_DATA, PARSE_SUCCESS, PARSE_ERROR, FORCE_FINISH, CHANGE_PARSER,
        VALIDATION_START, VALIDATION_FAILED, VALIDATION_SUCCESS
    }

}