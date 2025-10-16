package com.toh.wearossample.presentation.setting

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blankj.utilcode.util.ToastUtils
import com.toh.wearossample.base.BaseThemeViewModel

class SettingViewModel(context: Context) : BaseThemeViewModel() {

    fun updateUnitSettings() {
        ToastUtils.showShort("Update unit settings")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!
                SettingViewModel(application)
            }
        }
    }
}