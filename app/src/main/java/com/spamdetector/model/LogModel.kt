package com.spamdetector.model

import android.os.Build
import android.telephony.PhoneNumberUtils
import androidx.annotation.RequiresApi
import com.spamdetector.model.data.CallTypeEnum
import com.spamdetector.model.data.LogDelimiterEnum
import com.spamdetector.model.data.LogInfo
import com.spamdetector.tasks.GetLogTask
import com.spamdetector.utils.Preferences
import com.spamdetector.utils.PreferencesKey
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class LogModel {

    private var settingModel = SettingModel()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getLogs(phoneNum: String, callback: ((Any?)->(Unit))?)
    {
        val unknownBlock = settingModel.getBlockPref(PreferencesKey.blockUnknownCallKey)
        var unknownType = "UnknownBlockOn"
        if(!unknownBlock)
            unknownType = "UnknownBlockOff"

        val data = JSONObject()
        data.put("formatNumber", PhoneNumberUtils.formatNumber(phoneNum, Locale.getDefault().country))
        data.put("unknownblock", unknownType)

        val list = ArrayList<String>()
        list.add("http://serverpc.iptime.org:8080/log/get")
        list.add(data.toString())

        GetLogTask().execute(list, null, callback)
    }

    fun saveLogs(logInfo: LogInfo, delimiter: Int, phoneNum : String="", callback: ((String?) -> Unit)?=null)
    {
        val data = JSONObject()
        data.put("formatNumber",PhoneNumberUtils.formatNumber(phoneNum,Locale.getDefault().country))
        data.put("clientName",logInfo.name)
        when(delimiter)
        {
            LogDelimiterEnum.전화기록.ordinal -> data.put("delimiter",1)
            LogDelimiterEnum.문자기록.ordinal -> data.put("delimiter",2)
            else -> data.put("delimiter",1)
        }
        when(logInfo.callType)
        {
            CallTypeEnum.수신 -> data.put("callType",1)
            CallTypeEnum.부재 -> data.put("callType",2)
            CallTypeEnum.거절 -> data.put("callType",3)
            CallTypeEnum.default -> data.put("callType",4)
            CallTypeEnum.발신 -> data.put("callType",5)
            else -> data.put("callType",1)
        }

        val list = ArrayList<String>()
        list.add("http://serverpc.iptime.org:8080/log/save")
        list.add(data.toString())

//        SaveLogTask().execute(list, null, callback)
    }

    fun setSmsLogId(id : Int) {
        Preferences.setInt(PreferencesKey.SMS_LOG_ID, id)
    }
    fun getSmsLogId() : Int = Preferences.getInt(PreferencesKey.SMS_LOG_ID)

    fun setCallLogId(id : Int) {
        Preferences.setInt(PreferencesKey.CALL_LOG_ID, id)
    }
    fun getCallLogId() : Int = Preferences.getInt(PreferencesKey.CALL_LOG_ID)
}