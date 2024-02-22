package com.kiestex.kinesteXb2bkotlin

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kiestex.KinesteXb2bkotlin.R

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var isLoading: MutableLiveData<Boolean>
    private lateinit var viewModel: ContentViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.web_view)
        isLoading = MutableLiveData()

        viewModel = ViewModelProvider(this)[ContentViewModel::class.java]

        viewModel.message.observe(this) { message ->
            viewModel.handle(message)
        }

        viewModel.showWebView.observe(this) {
            if (it.equals(State.ERROR)) {
                finish()
            }
        }

        isLoading.observe(this) { loading ->
            if (loading) {
                // WebView is loading
            } else {
                // WebView finished loading
            }
        }

        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = MyWebViewClient()
        webView.settings.javaScriptEnabled = true

        webView.addJavascriptInterface(WebAppInterface(), "listener")

        webView.loadUrl("https://kineste-x-w.vercel.app/")
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            isLoading.postValue(false) // WebView finished loading
            viewModel.showWebView.postValue(State.SUCCESS.name)
            // Add more parameters here like height, age, weight
            val script = """
         window.postMessage({
           'key': '13c5398cf7a98e3469f6fc8a9a5b2b9d5c8a4814',
           'company': 'KinesteX',
           'userId': 'Shahruh',
           'planC': 'Cardio',
           'category': 'Fitness'});
        """.trimIndent()

            // Pass the values
            view?.evaluateJavascript(script) { result ->
                if (result != null) {
                    // Handle result if needed
                }
            }
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            Log.e("WebAppInterface", "postMessage: $message")
            viewModel.message.postValue(message)
        }
    }
}