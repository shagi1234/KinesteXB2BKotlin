package com.kiestex.webviewlib

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kiestex.webviewlib.repository.MessageCallback


/*
 * Created by shagi on 25.02.2024 01:56
 */

class KinesteXWebView(
    context: Context,
    private val apiKey: String,
    private val companyName: String,
    private val userId: String,
    private val planCategory: String,
    private val workoutCategory: String,
    private val messageCallback: MessageCallback
) {
    var webView: WebView = WebView(context).apply {
        setupWebView(this)
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWebView(webView: WebView) {
        with(webView) {
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    injectJavaScript(view)
                }
            }
            settings.javaScriptEnabled = true
            addJavascriptInterface(object {
                @JavascriptInterface
                fun postMessage(message: String) {
                    messageCallback.onMessageReceived(message)
                }
            }, "messageHandler")
            loadUrl("https://kineste-x-w.vercel.app")
        }
    }

    private fun injectJavaScript(view: WebView?) {
        val script = """
            window.postMessage({
                'apiKey': '$apiKey',
                'company': '$companyName',
                'userId': '$userId',
                'planCategory': '$planCategory',
                'workoutCategory': '$workoutCategory'
            });
        """.trimIndent()
        view?.evaluateJavascript(script, null)
    }


    companion object {
        @JvmStatic
        fun createWebView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            planCategory: String,
            workoutCategory: String,
            callback: MessageCallback
        ): KinesteXWebView {
            return KinesteXWebView(context, apiKey, companyName, userId, planCategory, workoutCategory, callback)
        }
    }
}