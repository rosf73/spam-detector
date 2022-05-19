package com.spamdetector.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.spamdetector.R
import com.spamdetector.model.LogModel
import com.spamdetector.model.SettingModel
import com.spamdetector.model.data.LogInfo
import com.spamdetector.tasks.MyAsyncTask
import com.spamdetector.utils.CallLogManager
import com.spamdetector.utils.Preferences
import java.util.*
import kotlin.collections.ArrayList

class CallingService : Service() {
    companion object {
        private lateinit var windowManager: WindowManager
        private lateinit var parentView : View
        private var toggle = false
        private lateinit var phoneNum : String
        private var networkOn :Boolean = false
    }

    private lateinit var nameView : TextView
    private lateinit var closeButton : ImageView
    private lateinit var acceptButton : LinearLayout
    private lateinit var rejectButton : LinearLayout
    private lateinit var logLoading : TextView
    private lateinit var logDone : LinearLayout
    private lateinit var todayLogInfo : LinearLayout
    private lateinit var logCountView : TextView
    private lateinit var revView : RecyclerView
    private lateinit var param :WindowManager.LayoutParams

    private lateinit var context : Context
    private var settingModel = SettingModel()
    private var logModel = LogModel()

    private var listToggle = false

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        Preferences.init(context)

        // Android O 이상일 경우 Foreground 서비스를 실행
        // Notification channel 설정.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val strId = getString(R.string.channel_id)
            val strTitle = getString(R.string.app_name)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var channel = notificationManager.getNotificationChannel(strId)
            if (channel == null) {
                channel = NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }
            val notification: Notification = NotificationCompat.Builder(this, strId).build()
            startForeground(1, notification)
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        phoneNum = intent.getStringExtra("call_number").toString()
        networkOn = intent.getBooleanExtra("network_check", false)

        val pref = settingModel.getShowPopUpPref()
        if (!pref) // 팝업 보기 설정이 꺼져있으면
            return START_REDELIVER_INTENT
        showView(phoneNum)

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? { return null }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDestroy() {
        stopForeground(true) // Foreground service 종료
        try {
            if(toggle) {
                windowManager.removeView(parentView)
                toggle = false
            }
        }
        catch(e: Exception) { }
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission", "ClickableViewAccessibility")
    private fun showView(number: String) {
        try
        {
            Preferences.init(applicationContext)

            val wrapper = object : FrameLayout(this) {
                override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                    return if (event.keyCode === KeyEvent.KEYCODE_BACK) {
                        true
                    } else super.dispatchKeyEvent(event)
                }
            }

            param  = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        or WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT
            )
            val li : LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            parentView = li.inflate(R.layout.activity_transparent, wrapper)
            closeButton = parentView.findViewById(R.id.delete_popup_btn)
            acceptButton = parentView.findViewById(R.id.acceptButton)
            rejectButton = parentView.findViewById(R.id.rejectButton)
            logLoading = parentView.findViewById(R.id.log_loading)

            val telecomManager = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val phoneNumber = PhoneNumberUtils.formatNumber(number, Locale.getDefault().country)

            windowManager.addView(parentView, param)
            toggle = true

            acceptButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    telecomManager.acceptRingingCall()
            }
            rejectButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    telecomManager.endCall()
            }
            closeButton.setOnClickListener {
                if (toggle)
                {
                    windowManager.removeView(parentView)
                    toggle = false
                }
                stopService(Intent(applicationContext, CallingService::class.java))
            }
            parentView.setOnTouchListener(object : View.OnTouchListener {
                var initialYTouch = 0f
                var initialY = 0
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event!!.action == MotionEvent.ACTION_DOWN) {
                        initialYTouch = event.rawY
                        initialY = param.y
                    } else if (event.action == MotionEvent.ACTION_MOVE) {
                        var y = initialY + event.rawY - initialYTouch
                        param.y = y.toInt()
                        windowManager.updateViewLayout(parentView, param);
                    }
                    return true
                }
            })

            logLoading.visibility = View.VISIBLE // 잠시만 기다려주세요 띄우기
            logDone.visibility = View.GONE
            class PopupTask: MyAsyncTask<String, String?>() {
                override fun onPreExecute() { }
                override fun doInBackground(arg: String?): String? {
                    return  CallLogManager.getContactNameByNumber(context, number)
                }
                override fun onPostExecute(result: String?) {
                    if (result != "-")
                        nameView.text = result

                    val callback = { result: Any? ->
                        logLoading.visibility = View.GONE // 잠시만 기다려주세요 제거
                        logDone.visibility = View.VISIBLE
                        if (settingModel.getShowPopUpPref() and !toggle)
                            toggle = true

                        if (result is String) {
                            when (result) {
                                "unknown block" -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        telecomManager.endCall()
                                        stopService(
                                            Intent(
                                                applicationContext,
                                                CallingService::class.java
                                            )
                                        )
                                    }
                                }
                                "no data" -> {
                                    val array = CallLogManager.getDeviceTodayCall(
                                        context,
                                        phoneNumber.replace("-", "")
                                    )
                                    logCountView.text = "(${array.size})"

                                    if (array.size <= 5) {
                                        val params = revView.layoutParams
                                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                                        revView.layoutParams = params
                                    } else {
                                        val params = revView.layoutParams
                                        val height: Int = TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP,
                                            275F,
                                            resources.displayMetrics
                                        ).toInt()
                                        params.height = height
                                        revView.layoutParams = params
                                    }
                                }
                                "error" -> {
                                    logLoading.visibility = View.VISIBLE
                                    logLoading.text = "서버 내부 오류로 조회가 불가능합니다"
                                    logDone.visibility = View.GONE
                                }
                                "else" -> {
                                    logLoading.visibility = View.VISIBLE
                                    logLoading.text = "서버 내부 오류로 조회가 불가능합니다"
                                    logDone.visibility = View.GONE
                                }
                            }
                        } else {

                        }
                    }
                    logModel.getLogs(phoneNumber, callback)
                }
            }
            if (networkOn) { // Network 있는 경우
                PopupTask().execute("", null, null)
            } else {
                class NotNetworkPopupTask : MyAsyncTask<String, String?>() {
                    override fun onPreExecute() {
                        logLoading.visibility = View.VISIBLE
                        logLoading.text = "네트워크 확인필요."
                        logDone.visibility = View.GONE
                    }
                    override fun doInBackground(arg: String?): String? {
                        return CallLogManager.getContactNameByNumber(context, number)
                    }

                    override fun onPostExecute(result: String?) {
                        if (settingModel.getShowPopUpPref() and !toggle)
                            toggle = true
                    }
                }

                NotNetworkPopupTask().execute("", null,null)
            }

            logCountView.text = "(0)"

            todayLogInfo.setOnClickListener {
                listToggle = !listToggle
                if(listToggle)
                    revView.visibility = View.VISIBLE
                else
                    revView.visibility = View.GONE
            }
        }
        catch(e:Exception)
        {
            logLoading.visibility = View.VISIBLE
            logLoading.text = "오류로 사용이 불가능합니다."
            logDone.visibility = View.GONE
            closeButton.setOnClickListener {
                if (toggle)
                {
                    windowManager.removeView(parentView)
                    toggle = false
                }
                stopService(Intent(applicationContext, CallingService::class.java))
            }
            parentView.setOnTouchListener(object : View.OnTouchListener {
                var initialYTouch = 0f
                var initialY = 0
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event!!.action == MotionEvent.ACTION_DOWN) {
                        initialYTouch = event.rawY
                        initialY = param.y
                    } else if (event.action == MotionEvent.ACTION_MOVE) {
                        var y = initialY + event.rawY - initialYTouch
                        param.y = y.toInt()
                        windowManager.updateViewLayout(parentView, param);
                    }
                    return true
                }
            })
        }
    }
}