package com.spamdetector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spamdetector.dialog.NetworkCheckDialog

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    abstract fun initPresenter()
    abstract fun initView()

    private fun checkNetwork(): Boolean {
        val intent = Intent(applicationContext, NetworkCheckDialog::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        return true
    }
}