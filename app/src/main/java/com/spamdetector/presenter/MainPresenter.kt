package com.spamdetector.presenter

import android.content.Context
import com.spamdetector.contract.MainContract

class MainPresenter : BasePresenter<MainContract.View>, MainContract.Presenter {

    private var mainView: MainContract.View? = null

    override fun takeView(view: MainContract.View) { mainView = view }
    override fun dropView() { mainView = null }

    override fun setRecentCallList(context: Context) {

    }
    override fun doSearch(str: String) {

    }
}