package com.spamdetector.model.data

data class LogInfo(
    var name: String="알수없음",
    var callNumber: String="",
    var date: String="",
    var time: String="",
    var delimiter: LogDelimiterEnum = LogDelimiterEnum.default,
    var callType: CallTypeEnum = CallTypeEnum.default
)