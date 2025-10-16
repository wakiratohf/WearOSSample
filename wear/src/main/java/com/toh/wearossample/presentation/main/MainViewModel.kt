package com.toh.wearossample.presentation.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.google.android.gms.wearable.Node
import com.toh.shared.WearableActionPath.PING_PHONE
import com.toh.shared.utils.LocaleManager
import com.toh.wearossample.WearApplicationModules
import com.toh.wearossample.base.BaseThemeViewModel
import com.toh.wearossample.eventbus.Event
import com.toh.wearossample.eventbus.MessageEventBus
import com.toh.wearossample.presentation.ScreenRoute
import com.toh.wearossample.utils.WearableUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(context) as T
    }
}

class MainViewModel(context: Context) : BaseThemeViewModel() {
    private val appContext = context.applicationContext
    private val appPref by lazy { WearApplicationModules.instant.getPreferencesHelper(appContext) }

    // State lưu trữ Locale hiện tại
    private val _currentLocale = MutableStateFlow(LocaleManager.getLocale(context.resources))
    val currentLocale: StateFlow<Locale> = _currentLocale

    private val _currentSettingData = MutableStateFlow("")
    val currentSettingData: StateFlow<String> = _currentSettingData

    private val _pairedDevices = MutableStateFlow<List<Node>>(emptyList())
    val pairedDevices: StateFlow<List<Node>> = _pairedDevices

    override fun languageChanged(newLocale: Locale) {
        _currentLocale.value = newLocale
    }

    fun pingPhone() {
        getPairedNodes()
        viewModelScope.launch(IODispatchers) {
            WearableUtils.sendCustomMessageToPhone(appContext, PING_PHONE)
        }
    }

    override fun onAppSettingChanged() {
        super.onAppSettingChanged()
        _currentSettingData.value = appPref.getAppSettings()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPingPhoneReply(messageEvent: MessageEventBus) {
        if (messageEvent.event == Event.PING_PHONE_REPLY) {
            (messageEvent.extraValue as? String)?.let {
                ToastUtils.showShort("Phone reply: \n$it")
            }
        }
    }

    fun init() {
        getPairedNodes()
    }

    private fun getPairedNodes() {
        viewModelScope.launch(IODispatchers) {
            _pairedDevices.value = WearableUtils.getPhonePairedList(appContext)
        }
    }
}