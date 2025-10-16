package com.toh.wearossample.helper.wearable

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.blankj.utilcode.util.GsonUtils
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.toh.shared.WearableActionPath
import com.toh.shared.WearableDataParam
import com.toh.shared.config.AppSetting
import com.toh.shared.utils.LocaleManager
import com.toh.wearossample.BaseApplication
import com.toh.wearossample.data.ApplicationModules
import com.utility.DebugLog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.greenrobot.eventbus.EventBus
import java.util.Locale

class WearableHelper(val context: Context) : DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {
    private var dataClient: DataClient? = null
    private var messageClient: MessageClient? = null
    private val appPref by lazy { ApplicationModules.instant.getPreferencesHelper(context.applicationContext) }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
    }

    fun addClientListener() {
        try {
            if (dataClient == null) {
                dataClient = Wearable.getDataClient(context.applicationContext)
            }
            dataClient?.addListener(this)
            if (messageClient == null) {
                messageClient = Wearable.getMessageClient(context.applicationContext)
            }
            messageClient?.addListener(this)
        } catch (e: Exception) {
            DebugLog.loge(e)
        }
    }

    fun removeClientListener() {
        dataClient?.removeListener(this)
        messageClient?.removeListener(this)
    }

    /**
     * Fix TH app wear vẫn mở, user vào settings device đổi language -> app trên wear không nhận được event để refresh language
     * */
    fun checkLocaleChanged(context: Context) {
        val preferencesHelper = ApplicationModules.instant.getPreferencesHelper(context)
        val localLanguage = Locale.getDefault().language
        val lastSyncAutoLanguage = preferencesHelper.getLastAutoLanguageForWear()
        if (lastSyncAutoLanguage.isEmpty() || localLanguage != lastSyncAutoLanguage) {
            DebugLog.loge("lastSyncAutoLanguage changed => doOnSyncAppSetting")
            preferencesHelper.setLastAutoLanguageForWear(localLanguage)
            ApplicationModules.instant.wearableHelper?.doOnSyncAppSetting()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendChangeEvent(actionPath: String, data: DataMap, alwaysPush: Boolean = false) {
        if (dataClient == null) return
        GlobalScope.launch(Dispatchers.IO + BaseApplication.coroutineExceptionHandler()) {
            val request = PutDataMapRequest.create(actionPath).apply {
                dataMap.putAll(data)
                if (alwaysPush) {
                    // Always push data even it's the same data
                    dataMap.putLong(WearableDataParam.TS, System.currentTimeMillis())
                }
            }.asPutDataRequest().setUrgent()
            dataClient?.putDataItem(request)?.await()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("LogNotTimber")
    override fun onMessageReceived(messageEvent: MessageEvent) {
        DebugLog.logd("onMessageReceived: ${messageEvent.path}")
        EventBus.getDefault().post(WearableMessageEvent(messageEvent.path, String(messageEvent.data)))
        if (messageEvent.path == WearableActionPath.SYNC_APP_SETTING) {
            doOnSyncAppSetting()
        } else if (messageEvent.path == WearableActionPath.PING_PHONE) {
            doOnPingPhone()
        }
    }

    fun doOnPingPhone() {
        sendChangeEvent(WearableActionPath.PING_PHONE, DataMap().apply {
            putString(WearableDataParam.MESSAGE, Build.MODEL + " pong at " + System.currentTimeMillis())
        }, true)
    }

    fun doOnSyncAppSetting() {
        sendChangeEvent(WearableActionPath.SYNC_APP_SETTING, DataMap().apply {
            var language = LocaleManager.getLanguage(context)
            if (language == LocaleManager.MODE_AUTO) {
                language = Locale.getDefault().language
            }
            putString(
                WearableDataParam.APP_SETTING_DATA, GsonUtils.toJson(
                    AppSetting(
                        language = language
                    )
                )
            )
        }, true)
    }

    class WearableMessageEvent(val path: String, val message: String? = null)

}
