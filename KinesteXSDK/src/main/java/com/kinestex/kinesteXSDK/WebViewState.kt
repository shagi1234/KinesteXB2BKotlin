package com.kinestex.kinesteXSDK

import android.webkit.WebView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WebViewState : ViewModel() {
    val webView: MutableLiveData<WebView?> = MutableLiveData(null)
}