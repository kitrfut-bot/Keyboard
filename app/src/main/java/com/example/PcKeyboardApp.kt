package com.example

import android.app.Application
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class PcKeyboardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupUncaughtExceptionHandler()
    }

    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()

                val crashLog = CrashLog(
                    timestamp = System.currentTimeMillis(),
                    threadName = thread.name ?: "Unknown Thread",
                    throwableName = throwable.javaClass.name,
                    message = throwable.localizedMessage ?: throwable.message,
                    stackTrace = stackTrace
                )

                Log.e("PcKeyboardApp", "Uncaught exception caught: ${throwable.message}\n$stackTrace")

                // Execute synchronous insert to ensure it is written before process death
                AppDatabase.getDatabase(this).crashLogDao().insertSync(crashLog)
            } catch (e: Exception) {
                Log.e("PcKeyboardApp", "Failed to save crash log to database", e)
            } finally {
                // Pass along to system / default handler
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
