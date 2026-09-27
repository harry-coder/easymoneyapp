package com.tech.easymoney.utils

import android.text.TextUtils
import android.util.Log

/**
 * Variant: debug
 * Full logging enabled, gated by FCConstants.IS_DISPLAY_LOGS.
 */
object LoggerUtils {

    @JvmStatic
    fun logVerbose(key: String, value: String?) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            Log.v(key, value ?: "")
        }
    }

    @JvmStatic
    fun logDebug(key: String, value: String?) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            Log.d(key, value ?: "")
        }
    }

    @JvmStatic
    fun logInfo(key: String, value: String?) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            Log.i(key, value ?: "")
        }
    }

    @JvmStatic
    fun logWarn(key: String, value: String?) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            Log.w(key, value ?: "")
        }
    }

    @JvmStatic
    fun logErr(key: String, value: String?) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            Log.e(key, value ?: "")
        }
    }

    @JvmStatic
    fun logErr(key: String, value: String?, t: Throwable) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            Log.e(key, value, t)
        }
    }

    @JvmStatic
    fun logDeviceIdentifierError(t: Throwable) {
        Log.e("DeviceIdentifier", t.message, t)
    }

    @JvmStatic
    fun logWtf(key: String, value: String?) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            Log.wtf(key, value)
        }
    }

    @JvmStatic
    fun logStackTrace(exception: Throwable) {
         exception.printStackTrace()
    }
}

