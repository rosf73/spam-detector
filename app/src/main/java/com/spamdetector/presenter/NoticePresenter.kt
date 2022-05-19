package com.spamdetector.presenter

import com.spamdetector.contract.NoticeContract
import com.spamdetector.model.data.NoticeInfo

class NoticePresenter : BasePresenter<NoticeContract.View>, NoticeContract.Presenter {
    private var noticeView: NoticeContract.View? = null

    override fun getNotice() {

    }

    override fun takeView(view: NoticeContract.View) {
        noticeView = view
    }

    override fun dropView() {
        noticeView = null
    }
}