package com.spamdetector.presenter

import android.content.Context
import com.spamdetector.contract.MainContract
import com.spamdetector.dialog.LoadingDialog

class MainPresenter : BasePresenter<MainContract.View>, MainContract.Presenter {

    private var mainView: MainContract.View? = null

    override fun takeView(view: MainContract.View) { mainView = view }
    override fun dropView() { mainView = null }

    override fun setRecentCallList(context: Context) {

    }
    override fun doSearch(str: String) {
        mainView?.showLoading(LoadingDialog("정보를 검색중이에요"))
        mainView?.showResult(null)
    }
}