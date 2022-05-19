package com.spamdetector.contract

import android.content.Context
import com.spamdetector.presenter.BasePresenter

interface MainContract {
    interface View {
        fun initPref()
        fun search(str: String)
    }
    interface Presenter : BasePresenter<View> {
        fun setRecentCallList(context: Context)
        fun doSearch(str: String)
    }
}