package com.spamdetector

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import com.spamdetector.adapters.NoticeListAdapter
import com.spamdetector.contract.NoticeContract
import com.spamdetector.databinding.ActivityNoticeBinding
import com.spamdetector.dialog.LoadingDialog
import com.spamdetector.model.data.NoticeInfo
import com.spamdetector.presenter.NoticePresenter

class NoticeActivity : BaseActivity(), NoticeContract.View {

    private lateinit var binding: ActivityNoticeBinding
    private lateinit var adapter: NoticeListAdapter
    private lateinit var noticePresenter: NoticeContract.Presenter
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPresenter()
        initView()

        noticePresenter.getNotice()
    }
    override fun onBackPressed() {
        finish()
    }

    override fun initPresenter() {
        noticePresenter = NoticePresenter()
    }
    override fun initView() {
        noticePresenter.takeView(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        binding.backBtn.setOnClickListener {
            finish()
        }

        adapter = NoticeListAdapter().apply {
            onClick = this@NoticeActivity::startNoticeActivity
        }
    }

    override fun showLoading() {
        loadingDialog = LoadingDialog("공지사항을 불러올게요")
        loadingDialog.show(supportFragmentManager, loadingDialog.tag)
    }
    override fun initList(list: ArrayList<NoticeInfo>) {
        binding.noticeList.adapter = adapter
        binding.noticeList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.bottom = 10
            }
        })
        loadingDialog.dismiss()
    }

    private fun startNoticeActivity(data: NoticeInfo) {
        startActivity(
            Intent(this, NoticeDetailActivity::class.java)
                .apply {
                    putExtra(NoticeDetailActivity.TITLE, data.title)
                    putExtra(NoticeDetailActivity.TITLE, data.title)
                    putExtra(NoticeDetailActivity.TITLE, data.title)
                }
        )
    }
}