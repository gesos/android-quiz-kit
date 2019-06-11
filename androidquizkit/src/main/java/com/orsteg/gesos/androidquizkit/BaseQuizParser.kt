package com.orsteg.gesos.androidquizkit

import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

abstract class BaseQuizParser {

    abstract val headerByteSize: Int

    var mNewParser: String = ""
    var mState: State = State.IDLE
    var mCallerState: State = State.IDLE
    var mBuffer: StringBuffer = StringBuffer()
    var mPointer: Int = 0
    var shouldValidate: Boolean = true

    var result: Deferred<State>? = null

    fun append(data: String): State {

        setCallerState(State.READ_START)

        if (arrayOf(State.VALIDATION_FAILED, State.PARSE_ERROR,
                State.FORCE_FINISH).contains(mState)) return mState

        mBuffer.append(data)

        if (mBuffer.length >= headerByteSize && result == null) {
            startParsing()
        }

        return mState
    }

    fun startParsing() {
        setState(State.VALIDATION_START)

        result = GlobalScope.async {
            if (shouldValidate && !validate()) {
                setState(State.VALIDATION_FAILED)
            } else {
                setState(State.VALIDATION_SUCCESS)

                while (!arrayOf(State.PARSE_ERROR, State.PARSE_SUCCESS,
                        State.FORCE_FINISH).contains(mState) && mCallerState == State.CANCELLED) {

                    mPointer = parse(mPointer)

                    if (mCallerState == State.END_OT_READ && mPointer == mBuffer.length) {
                        setState(State.PARSE_SUCCESS)
                    }
                }
            }
            mState
        }
    }

    suspend fun finish(): State {

        if (result == null) {
            startParsing()
        }

        setCallerState(State.END_OT_READ)

        if (arrayOf(State.PARSE_SUCCESS, State.FORCE_FINISH).contains(result?.await())) {
            onFinish()
        } else {
            onFailed()
        }

        return mState
    }


    fun cancel() {
        mCallerState = State.CANCELLED
        if (result?.isActive == true) result?.cancel()
        onCancel()
    }

    fun setState(state: State) {
        if (mState != state) mState = state
    }

    fun setCallerState(state: State) {
        if (mCallerState != state) mCallerState = state
    }

    fun copy(parser: BaseQuizParser) {
        parser.mBuffer = mBuffer
    }

    open fun reset() {
        mNewParser = ""
        mPointer = 0
        mBuffer = StringBuffer()
        mState = State.IDLE
        mCallerState = State.IDLE
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
        IDLE, CANCELLED, END_OT_READ, PARSE_SUCCESS, PARSE_ERROR, FORCE_FINISH, CHANGE_PARSER,
        VALIDATION_START, VALIDATION_FAILED, VALIDATION_SUCCESS, READ_START
    }

}