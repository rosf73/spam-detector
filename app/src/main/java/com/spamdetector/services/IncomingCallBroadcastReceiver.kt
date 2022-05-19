package com.spamdetector.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.spamdetector.model.SettingModel
import com.spamdetector.utils.NetworkManager
import com.spamdetector.utils.Preferences
import com.spamdetector.utils.PreferencesKey

class IncomingCallBroadcastReceiver: BroadcastReceiver() {

    private var settingModel = SettingModel()
    var isRegistered = true
    var networkOn = true

    fun register() { isRegistered = true }
    fun unregister() { isRegistered = false }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (!NetworkManager.checkNetwork(context)) // 네트워크에 연결되지 않은 경우
            networkOn = false

        Preferences.init(context)
        val phoneNum = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        if (phoneNum.equals(null)) // 중복 실행 이슈 시, 첫 실행에 NULL 값이 반환됨을 처리 // 두 번째 실행(정상)
            return
        if (settingModel.getBlockPref(PreferencesKey.blockTodayCallKey)) // 오늘 문의 모두 차단
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager).endCall()
                return
            }

        val serviceIntent = Intent(context, CallingService::class.java)
        serviceIntent.putExtra("call_number", phoneNum.toString())
        when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
            TelephonyManager.EXTRA_STATE_RINGING -> { // 전화 울림
                if(!networkOn)
                    serviceIntent.putExtra("network_check", false)
                else
                    serviceIntent.putExtra("network_check", true)
                context.startForegroundService(serviceIntent)
            }
            TelephonyManager.EXTRA_STATE_IDLE -> { // 전화 끊김(수신, 발신, 부재 여부 상관 없이)
                context.stopService(serviceIntent)
            }
        }
    }
}