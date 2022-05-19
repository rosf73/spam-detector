package com.spamdetector.contract

import android.content.Context
import com.spamdetector.dialog.LoadingDialog
import com.spamdetector.model.data.ClientInfo
import com.spamdetector.presenter.BasePresenter

interface MainContract {
    interface View {
        fun initPref()
        fun search(str: String)
        fun showResult(clientInfo: ClientInfo?)

        fun showLoading(dialog: LoadingDialog)
        fun dismissLoading()
    }
    interface Presenter : BasePresenter<View> {
        fun setRecentCallList(context: Context)
        fun doSearch(str: String)
    }
}