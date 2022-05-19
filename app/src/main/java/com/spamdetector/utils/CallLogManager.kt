package com.spamdetector.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import com.spamdetector.model.data.CallTypeEnum
import com.spamdetector.model.data.LogDelimiterEnum
import com.spamdetector.model.data.LogInfo
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CallLogManager { companion object {

    private fun getNumberTodayCallLog(context: Context, number: String) : ArrayList<LogInfo> {
        val data = ArrayList<LogInfo>()

        val managedCursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            CallLog.Calls.NUMBER + "= ? ",
            arrayOf(number),
            CallLog.Calls.DATE + " DESC"
        )

        val cachedNameIndex = managedCursor?.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val date = managedCursor?.getColumnIndex(CallLog.Calls.DATE)
        val type: Int? = managedCursor?.getColumnIndex(CallLog.Calls.TYPE)

        loop@while (managedCursor!!.moveToNext()) {
            val callDate = managedCursor.getLong(date!!);
            var cachedName: String? = managedCursor.getString(cachedNameIndex!!)
            if(cachedName==null)
                cachedName = "-"

            val date = Date(callDate)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dateString:String = format.format(callDate)
            val dateSplit = dateString.split(" ")
            if(!UtilManager.checkToday(dateString)) // 오늘 전화가 아니면 break
                break@loop
            val type: String = managedCursor.getString(type!!)
            var callTypeEnum : CallTypeEnum = CallTypeEnum.수신
            when (type.toInt()) {
                CallLog.Calls.OUTGOING_TYPE -> callTypeEnum = CallTypeEnum.발신
                CallLog.Calls.INCOMING_TYPE -> callTypeEnum = CallTypeEnum.수신
                CallLog.Calls.MISSED_TYPE -> callTypeEnum = CallTypeEnum.부재
            }
            data.add(
                LogInfo(
                    name = cachedName.toString(),
                    callNumber = number,
                    date = dateSplit[0],
                    time = dateSplit[1],
                    delimiter = LogDelimiterEnum.전화기록,
                    callType = callTypeEnum
                )
            )
        }
        return data
    }
    private fun getNumberTodaySmsLog(context: Context, number: String) : ArrayList<LogInfo>
    {
        val data = ArrayList<LogInfo>()

        val  managedCursor:Cursor? = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            "address=?",
            arrayOf(number),
            "date DESC"
        )

        val dateIndex = managedCursor?.getColumnIndex(Telephony.Sms.DATE)
        val phoneNumIndex = managedCursor?.getColumnIndex(Telephony.Sms.ADDRESS)
        val typeIndex= managedCursor?.getColumnIndex(Telephony.TextBasedSmsColumns.TYPE);

        loop@while (managedCursor!!.moveToNext()) {
            val date: Long = managedCursor.getLong(dateIndex!!)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dateString:String = format.format(date)
            val dateSplit = dateString.split(" ")
            val protocol = managedCursor.getInt(typeIndex!!)
            var typeEnum : CallTypeEnum = CallTypeEnum.부재
            when(protocol)
            {
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT -> {
                    typeEnum = CallTypeEnum.발신
                }
                Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX -> {
                    typeEnum = CallTypeEnum.수신
                }
                else -> {
                    continue@loop
                }
            }
            if(!UtilManager.checkToday(dateString))
                break@loop
            val phoneNumber = managedCursor.getString(phoneNumIndex!!) //this is phone number rather than address
            val contact: String = getContactNameByNumber(context, phoneNumber)!! //call the metod that convert phone number to contact name in your contacts

            data.add(
                LogInfo(
                    name = contact,
                    callNumber = phoneNumber,
                    date = dateSplit[0],
                    time = dateSplit[1],
                    delimiter = LogDelimiterEnum.문자기록,
                    callType = typeEnum
                )
            )
        }
        return data
    }

    fun getDeviceTodayCall(context: Context, number: String): ArrayList<LogInfo> {
        var temp = number.replace("-", "")
        val data = ArrayList<LogInfo>()
        val callLog = ArrayList<LogInfo>(getNumberTodayCallLog(context, temp))
        val messageLog = ArrayList<LogInfo>(getNumberTodaySmsLog(context, temp))

        data.addAll(callLog)
        data.addAll(messageLog)

        val cmp = compareByDescending<LogInfo> { StringManager.changeStringToDate(it.date, it.time) }
        return sortLogs(data.sortedWith(cmp).toCollection(ArrayList()))
    }

    fun sortLogs(data: ArrayList<LogInfo>): ArrayList<LogInfo> {
        var tmp = ArrayList<LogInfo>()
        tmp.addAll(data)
        val cmp = compareByDescending<LogInfo> { SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("${it.date} ${it.time}") }       //정렬후
        tmp = tmp.sortedWith(cmp).distinctBy { listOf(it.callNumber, it.delimiter) }.toCollection(ArrayList())

        var callOrMessage = ArrayList<LogInfo>()
        var notCallOrMessage = ArrayList<LogInfo>()

        for(i in tmp)
        {
            if((i.delimiter== LogDelimiterEnum.전화기록) or (i.delimiter==LogDelimiterEnum.문자기록))
                callOrMessage.add(i)
            else
                notCallOrMessage.add(i)
        }
        return (callOrMessage + notCallOrMessage).toCollection(ArrayList())
    }

    fun getContactNameByNumber(context: Context, number: String): String {
        val uri: Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(
                number
            )
        );
        var displayName = "-"

        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null);
        val nameIndex = cursor?.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
        if(cursor != null){
            if(cursor.moveToFirst())
                displayName = cursor.getString(nameIndex!!)
            if(displayName.isEmpty())
                displayName = "-"
            cursor.close()
        }
        return displayName
    }
}}