package com.toh.wearossample.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blankj.utilcode.util.ToastUtils
import com.toh.shared.WearableBridgeTag
import com.toh.shared.utils.LocaleManager
import com.toh.wearossample.R
import com.toh.wearossample.data.ApplicationModules
import com.toh.wearossample.helper.NotificationHelper
import com.toh.wearossample.helper.wearable.WearableHelper.WearableMessageEvent
import com.toh.wearossample.utils.PermissionUtils
import com.toh.wearossample.utils.commons.Constants
import com.utility.DebugLog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity() {
    private var tvLog: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
    }

    private fun init() {
        checkWearableConnect()
        findViewById<View>(R.id.bt_sync_settings).setOnClickListener {
            ToastUtils.showShort("Start sync settings")
            ApplicationModules.instant.wearableHelper?.doOnSyncAppSetting()
        }
        findViewById<View>(R.id.bt_notification_with_bridge_home).setOnClickListener {
            if (PermissionUtils.isPostNotificationsPermissionGranted(this)) {
                NotificationHelper.showNotification(this@MainActivity, bridgeTag = WearableBridgeTag.Excluded.HOME)
            } else {
                PermissionUtils.requestPostNotificationsPermission(this@MainActivity)
            }
        }
        findViewById<View>(R.id.bt_notification_with_bridge_other).setOnClickListener {
            if (PermissionUtils.isPostNotificationsPermissionGranted(this)) {
                NotificationHelper.showNotification(this@MainActivity, bridgeTag = WearableBridgeTag.Pushable.OTHERS)
            } else {
                PermissionUtils.requestPostNotificationsPermission(this@MainActivity)
            }
        }
        checkLanguageChanged()
        tvLog = findViewById(R.id.tv_log)
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    fun onMessageEvent(event: WearableMessageEvent) {
        runOnUiThread {
            val ts = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis());
            tvLog?.text = "[$ts] " + event.path + (if (event.message.isNullOrEmpty()) "" else " => ${event.message}") + "\n" + tvLog?.text
        }
    }

    /**
     * Kiểm tra và thử connect lại với Wearable
     * */
    private fun checkWearableConnect() {
        if (ApplicationModules.instant.wearableManager?.isWearApiAvailable == false) {
            ApplicationModules.instant.wearableManager?.connect()
        }
    }

    private fun checkLanguageChanged() {
        if (intent != null && intent.hasExtra(Constants.EXTRA_LANGUAGE_CHANGED)) {
            intent.removeExtra(Constants.EXTRA_LANGUAGE_CHANGED)
            DebugLog.loge("EXTRA_LANGUAGE_CHANGED => doOnSyncAppSetting")
            ApplicationModules.instant.wearableHelper?.doOnSyncAppSetting()
        } else if (LocaleManager.getLanguage(this) == LocaleManager.MODE_AUTO) {
            ApplicationModules.instant.wearableHelper?.checkLocaleChanged(this@MainActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}