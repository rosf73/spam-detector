package com.spamdetector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import com.spamdetector.databinding.ActivityNoticeDetailBinding

class NoticeDetailActivity : AppCompatActivity() {
    companion object {
        const val TITLE = "TITLE"
        const val CONTENT = "CONTENT"
        const val DATE = "DATE"
    }

    private lateinit var binding: ActivityNoticeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.apply {
            backBtn.setOnClickListener { finish() }

            noticeTitle.text = intent.getStringExtra(TITLE)
            noticeDate.text = intent.getStringExtra(DATE)
            noticeContent.text = intent.getStringExtra(CONTENT)
            noticeContent.movementMethod = ScrollingMovementMethod()
        }
    }
    override fun onBackPressed() {
        finish()
    }
}