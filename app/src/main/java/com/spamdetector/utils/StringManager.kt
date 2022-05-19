package com.spamdetector.utils

import android.telephony.PhoneNumberUtils
import java.text.SimpleDateFormat
import java.util.*

class StringManager { companion object {
    fun changeToHyphenNumber(number: String): String {
        var temp = number
        if (temp[0] == '+') {
            val len = temp.split(" ")[0].length
            if (temp.length > len)
                temp = temp.substring(len)
        }

        var nums = temp.replace(("[^\\d.]").toRegex(), "") // 숫자만 추출
        if (nums.isEmpty()) return number
        if (nums[0] == '1') {
            nums = "0$nums"
        }
        return PhoneNumberUtils.formatNumber(nums, Locale.getDefault().country)
    }

    fun insertHyphen(str: String?): String {
        if (str!!.length == 3) { // 010 -> 010)
            return "$str)"
        } else if (str.length > 3) {
            val onlyNumber = str.replace(")", "").replace("-", "")

            if (onlyNumber.length == 6) return "$str-" // 010)000 -> 010)000-
            if (onlyNumber.length == 11) { // 010)000-00000 -> 010)0000-0000
                return "${onlyNumber.substring(0, 3)})${onlyNumber.substring(3, 7)}-${
                    onlyNumber.substring(
                        7
                    )
                }"
            }
        }
        return str
    }

    fun deleteHyphen(str: String?): String {
        val onlyNumber = str!!.replace(")", "").replace("-", "")
        if (onlyNumber.length == 10) { // 010)0000-000 -> 010)000-0000
            return "${onlyNumber.substring(0, 3)})${onlyNumber.substring(3, 6)}-${
                onlyNumber.substring(
                    6
                )
            }"
        } else if (onlyNumber.length == 6 && str.length == 7) { // 010)000 -> 010)00
            return str.substring(0, 6)
        } else if (str.length == 3) { // 010 -> 01
            return str.substring(0, 2)
        }
        return str
    }

    fun changeToCleanNumber(str: String): String {
        return str.replace(")", "-")
    }

    fun changeStringToDate(date: String, time: String) : Date?
    {
        val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return input.parse("$date $time")
    }
}}