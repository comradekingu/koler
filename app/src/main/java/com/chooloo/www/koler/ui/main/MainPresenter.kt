package com.chooloo.www.koler.ui.main

import android.content.Intent
import com.chooloo.www.koler.ui.base.BasePresenter
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class MainPresenter<V : MainContract.View> : BasePresenter<V>(), MainContract.Presenter<V> {
    override fun onDialpadFabClick() {
        mvpView?.openDialpad()
    }

    override fun onMenuClick() {
        mvpView?.openSettings()
    }

    override fun onViewIntent(intent: Intent) {
        val intentText = try {
            URLDecoder.decode(intent.dataString, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            mvpView?.showError("An error occurred when trying to get phone number :(")
            return
        }

        if (intentText.contains("tel:")) {
            mvpView?.apply {
                openDialpad()
                dialpadNumber = intentText
            }
        } else {
            mvpView?.showError("No phone number detected")
        }
    }

    override fun onSearchTextChanged(text: String) {
        mvpView?.apply {
            when (selectedPage) {
                0 -> liveContactsText = text
                1 -> liveRecentsText = text
            }
        }
    }

    override fun onPageSelected(position: Int) {
        mvpView?.apply {
            when (position) {
                0 -> searchText = liveContactsText
                1 -> searchText = liveRecentsText
            }
        }
    }

    override fun onDialpadTextChanged(text: String?) {
        mvpView?.updateSearchViewModelNumber(text)
    }
}