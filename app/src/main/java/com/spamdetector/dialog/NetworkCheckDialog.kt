package com.spamdetector.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import com.spamdetector.databinding.DialogCheckNetworkBinding
import com.spamdetector.utils.Preferences
import kotlin.system.exitProcess

class NetworkCheckDialog : Activity() {

    private lateinit var binding: DialogCheckNetworkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        Preferences.init(applicationContext)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding = DialogCheckNetworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeBtn.setOnClickListener {
            finishAffinity()
            exitProcess(0)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }

    override fun onBackPressed() {
    }
}