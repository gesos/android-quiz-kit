package com.orsteg.gesos.androidquizkit.builder

import android.support.annotation.RawRes
import java.io.File
import java.io.InputStream

class BuildMethod private constructor(val mMethod: Method) {

    var mFile: File? = null
        private set(value) {
            field = value
        }

    var mString: String? = null
        private set(value) {
            field = value
        }

    var mRes: Int? = null
        private set(value) {
            field = value
        }

    var mStream: InputStream? = null
        private set(value) {
            field = value
        }

    enum class Method{
        STREAM, RES, STR, URL, FILE
    }

    companion object {
        fun fromInputStream(stream: InputStream): BuildMethod {
            return BuildMethod(Method.STREAM)
                .apply { mStream = stream }
        }

        fun fromResource(@RawRes resId: Int): BuildMethod {
            return BuildMethod(Method.RES)
                .apply { mRes = resId }
        }

        fun fromFile(file: File): BuildMethod {
            return BuildMethod(Method.FILE)
                .apply { mFile = file }
        }

        fun fromString(text: String): BuildMethod {
            return BuildMethod(Method.STR)
                .apply { mString = text }
        }

        private fun fromUrl(url: String): BuildMethod {
            return BuildMethod(Method.URL)
                .apply { mString = url }
        }
    }
}
