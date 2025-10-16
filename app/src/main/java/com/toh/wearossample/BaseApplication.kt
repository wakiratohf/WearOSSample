@file:Suppress("KotlinConstantConditions")

package com.toh.wearossample
import android.annotation.SuppressLint
import android.app.Application
import com.blankj.utilcode.util.Utils
import com.toh.wearossample.data.ApplicationModules
import com.utility.BuildConfig
import com.utility.DebugLog
import kotlinx.coroutines.CoroutineExceptionHandler

class BaseApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: BaseApplication? = null
        val isTestMode = BuildConfig.DEBUG

        fun coroutineExceptionHandler() = instance?.coroutineExceptionHandler ?: CoroutineExceptionHandler { _, throwable ->
            DebugLog.loge(throwable)
        }
    }

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        DebugLog.loge(throwable)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        DebugLog.DEBUG = isTestMode // Đặt flag cho DebugLog (Chỉ hiển thị log trong bản build Debug | TEST_AD)
        Utils.init(this)
        ApplicationModules.instant.initModules(this)
    }


    override fun onTerminate() {
        super.onTerminate()
        ApplicationModules.instant.onDestroy()
    }
}