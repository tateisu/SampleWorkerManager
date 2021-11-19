package jp.juggler.sampleworkermanager

import android.util.Log

class LogTag(private val prefix: String) {
    companion object {
        const val tag = "SampleIntentService"
    }

    fun v(msg: String) = Log.v(tag, "$prefix: $msg")
    fun d(msg: String) = Log.d(tag, "$prefix: $msg")
    fun i(msg: String) = Log.i(tag, "$prefix: $msg")
    fun w(msg: String) = Log.w(tag, "$prefix: $msg")
    fun e(msg: String) = Log.e(tag, "$prefix: $msg")
    fun w(ex: Throwable?, msg: String) = Log.w(tag, "$prefix: $msg", ex)
    fun e(ex: Throwable?, msg: String) = Log.e(tag, "$prefix: $msg", ex)
}
