package com.spamdetector.presenter

interface BasePresenter<T> {
    fun takeView(view: T)
    fun dropView()
}