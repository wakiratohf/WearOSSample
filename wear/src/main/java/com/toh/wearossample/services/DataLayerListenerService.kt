package com.toh.wearossample.services

import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.toh.shared.WearableActionPath.PING_PHONE
import com.toh.shared.WearableActionPath.SYNC_APP_SETTING
import com.toh.shared.WearableDataParam
import com.toh.shared.config.AppSetting
import com.toh.shared.utils.LocaleManager
import com.toh.wearossample.WearApplicationModules
import com.toh.wearossample.eventbus.Event
import com.toh.wearossample.eventbus.MessageEventBus
import com.toh.wearossample.utils.WearableUtils
import com.utility.DataUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class DataLayerListenerService : WearableListenerService() {
    companion object {
        private const val TAG = "DataLayerListenerSvc"
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val appPref by lazy { WearApplicationModules.instant.getPreferencesHelper(this.applicationContext) }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            super.attachBaseContext(LocaleManager.setLocale(newBase))
        } ?: let {
            super.attachBaseContext(null)
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        dataEvents.forEach { dataEvent ->
            Log.d(TAG, "Data changed: ${dataEvent.dataItem.uri}")
            val uri = dataEvent.dataItem.uri
            when (uri.path) {
                SYNC_APP_SETTING -> {
                    onSyncAppSetting(dataEvent)
                }

                PING_PHONE -> {
                    doOnPingPhone(dataEvent)
                }
            }
        }
    }

    private fun doOnPingPhone(dataEvent: DataEvent) {
        DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.let { dataMap ->
            val pingResultMessage = dataMap.getString(WearableDataParam.MESSAGE)
            EventBus.getDefault().post(MessageEventBus(Event.PING_PHONE_REPLY, pingResultMessage))
        }
    }

    private fun onSyncAppSetting(dataEvent: DataEvent) {
        DataMapItem.fromDataItem(dataEvent.dataItem).dataMap.let { dataMap ->
            parseJsonOrNull<AppSetting>(dataMap.getString(WearableDataParam.APP_SETTING_DATA))?.let { appSetting ->
                var languageChanged = false
                appSetting.language?.let { language ->
                    val oldLanguage = LocaleManager.getLanguage(this@DataLayerListenerService)
                    if (oldLanguage != language) {
                        Log.e(TAG, "LANGUAGE_CHANGED: AppSettings language: $language, oldLanguage: $oldLanguage")
                        languageChanged = true
                        LocaleManager.setNewLocale(this@DataLayerListenerService, language)
                    }
                }

                // Check if app setting changed
                val oldAppSetting = appPref.getAppSettings()
                val newAppSetting = GsonUtils.toJson(appSetting)
                if (oldAppSetting != newAppSetting) {
                    EventBus.getDefault().post(MessageEventBus(Event.APP_SETTING_CHANGED))
                    Log.i(TAG, "Sync app setting from phone: $appSetting")
                }
                appPref.setAppSettings(appSetting)

                if (languageChanged) {
                    EventBus.getDefault().post(
                        MessageEventBus(
                            Event.LANGUAGE_CHANGED,
                            LocaleManager.getLocale(this@DataLayerListenerService.resources)
                        )
                    )
                } else {
                    ToastUtils.showShort("Sync successful!")
                }
            }
        }
    }

    private fun checkToSyncAppSetting() {
        ioScope.launch {
            val appSetting = appPref.getAppSettings()
            if (appSetting.isEmpty()) {
                WearableUtils.sendCustomMessageToPhone(this@DataLayerListenerService, SYNC_APP_SETTING)
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
    }

    override fun onDestroy() {
        super.onDestroy()
        ioScope.cancel()
    }

    private inline fun <reified T> parseJsonOrNull(json: String?): T? {
        if (json.isNullOrEmpty()) return null
        return try {
            DataUtils.parserObject(json, T::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON for type ${T::class.java.simpleName}: ${e.message}")
            null
        }
    }

}
