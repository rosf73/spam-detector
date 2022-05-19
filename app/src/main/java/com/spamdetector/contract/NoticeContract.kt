package com.spamdetector.contract

import com.spamdetector.model.data.NoticeInfo
import com.spamdetector.presenter.BasePresenter

interface NoticeContract {
    interface View {
        fun showLoading()
        fun initList(list: ArrayList<NoticeInfo>)
    }
    interface Presenter : BasePresenter<View> {
        fun getNotice()
    }
}