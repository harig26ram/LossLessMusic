package com.losslessmusic.app

import android.app.Application
import android.os.Environment
import java.io.File

class LossLessMusicApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                val logDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                if (logDir != null) {
                    File(logDir, "crash.log").appendText(
                        "${System.currentTimeMillis()}: ${e.message}\n${e.stackTraceToString()}\n"
                    )
                }
            } catch (_: Exception) {}
        }
    }
}
