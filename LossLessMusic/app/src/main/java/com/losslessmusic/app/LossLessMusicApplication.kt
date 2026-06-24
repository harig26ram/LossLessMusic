package com.losslessmusic.app

import android.app.Application
import android.os.Environment
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@HiltAndroidApp
class LossLessMusicApplication : Application() {

    private val logExecutor = Executors.newSingleThreadExecutor()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    override fun onCreate() {
        super.onCreate()
        installCrashHandler()
        Log.i("LossLessMusic", "Application started")
    }

    private fun installCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logCrash("UNCAUGHT", thread.name, throwable)
            try { Thread.sleep(100) } catch (_: InterruptedException) {}
            System.exit(1)
        }
    }

    fun logCrash(tag: String, thread: String, throwable: Throwable) {
        logExecutor.execute {
            try {
                val logDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
                val logFile = File(logDir, "crash.log")
                val timestamp = dateFormat.format(Date())
                val stackTrace = throwable.stackTrace.joinToString("\n") { "    at $it" }
                val cause = throwable.cause?.let { "\nCaused by: ${it.message}\n${it.stackTrace.joinToString("\n") { "    at $it" }}" } ?: ""

                val logEntry = """
                    ==================================================
                    [$timestamp] $tag crash on thread: $thread
                    Exception: ${throwable.javaClass.name}: ${throwable.message}
                    $stackTrace
                    $cause
                    ==================================================
                    """

                FileWriter(logFile, true).use { it.write(logEntry) }
                Log.e("LossLessMusic", "Crash logged to ${logFile.absolutePath}")
            } catch (e: Exception) {
                Log.e("LossLessMusic", "Failed to write crash log", e)
            }
        }
    }

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        logExecutor.execute {
            try {
                val logDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
                val logFile = File(logDir, "error.log")
                val timestamp = dateFormat.format(Date())
                val stackTrace = throwable?.stackTrace?.joinToString("\n") { "    at $it" } ?: ""
                val logEntry = "[$timestamp] $tag: $message\n$stackTrace\n---\n"
                FileWriter(logFile, true).use { it.write(logEntry) }
            } catch (_: Exception) {}
        }
    }
}
