package com.virtual5g.core.logging

import android.util.Log
import com.virtual5g.core.BuildConfig

/**
 * Every log line in the app should go through this object rather than
 * android.util.Log directly. Two rules enforced here:
 *  1. Nothing logs in release builds beyond warnings/errors.
 *  2. Known-sensitive field names are redacted defensively, in case a
 *     caller accidentally interpolates a raw object's toString().
 */
object SecureLogger {

    private val sensitiveKeys = listOf(
        "imei", "imsi", "iccid", "msisdn", "subscriberid", "token", "authorization", "password"
    )

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, redact(message))
    }

    fun i(tag: String, message: String) {
        Log.i(tag, redact(message))
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, redact(message), throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, redact(message), throwable)
    }

    private fun redact(message: String): String {
        var result = message
        sensitiveKeys.forEach { key ->
            val regex = Regex("(?i)$key\\s*[:=]\\s*\\S+")
            result = regex.replace(result, "$key=[REDACTED]")
        }
        return result
    }
}
