package com.losslessmusic.app.diag

import android.util.Log
import com.losslessmusic.app.LossLessMusicApplication

object CrashLogger {
    private var app: LossLessMusicApplication? = null

    fun init(application: LossLessMusicApplication) {
        app = application
    }

    fun e(tag: String, throwable: Throwable) {
        Log.e("LossLessMusic", "[$tag]", throwable)
        app?.let { it.logCrash(tag, Thread.currentThread().name, throwable) }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("LossLessMusic", "[$tag] $message", throwable)
        app?.let { it.logError(tag, message, throwable) }
    }

    fun w(tag: String, message: String) {
        Log.w("LossLessMusic", "[$tag] $message")
    }

    fun w(tag: String, throwable: Throwable) {
        Log.w("LossLessMusic", "[$tag]", throwable)
    }

    fun d(tag: String, message: String) {
        Log.d("LossLessMusic", "[$tag] $message")
    }
}
