package com.spamdetector.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UtilManager { companion object {

    fun copyToClip(context: Context, string: String) {
        val clipboard: ClipboardManager =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("number", string)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "클립보드에 복사했습니다", Toast.LENGTH_SHORT).show()
    }

    fun checkToday(dateString: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val zoneId = ZoneId.of("Asia/Seoul")
            var covertToLocalDate = changeToLocalDateTime(dateString)
            var convertDate = covertToLocalDate!!.atZone(ZoneId.of("Asia/Seoul"))
            val now = LocalDateTime.now()
            if(isBeforeStandardTime())      //당일 0시~ 8시 사이면 전날 새벽 8시~현재 시간까지 true
            {
                val standard = now.atZone(zoneId).minusDays(1).with(LocalTime.of(8,0,0,0))
                return convertDate!!.isAfter(standard)
            }
            else                            //새벽 8시 이후이면 다음날 00시 이전까지 true
            {
                val today = now.atZone(zoneId).with(LocalTime.of(8,0,0,0))
                val tomorrow = now.atZone(zoneId).plusDays(1).with(LocalTime.of(0,0,0,0))
                return (convertDate!!.isBefore(tomorrow) and convertDate.isAfter(today))
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeToLocalDateTime(dateString: String) : LocalDateTime?
    {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateString, formatter);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isBeforeStandardTime() : Boolean        // 아침 8시 이전 true, 아침 8시 이후 false
    {
        val today : LocalDateTime= LocalDateTime.now()
        val zoneTime = today.atZone(ZoneId.of("Asia/Seoul"))
        return ((zoneTime.hour < 8) and (zoneTime.hour >=0))
    }
}}