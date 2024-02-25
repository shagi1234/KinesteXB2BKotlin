package com.kiestex.kinesteXb2bkotlin

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.kiestex.KinesteXb2bkotlin.R
import com.kiestex.webviewlib.KinesteXWebView
import com.kiestex.webviewlib.repository.MessageCallback

class MainActivity : AppCompatActivity() {
    private lateinit var isLoading: MutableLiveData<Boolean>
    private lateinit var viewModel: ContentViewModel
    private lateinit var kinesteXWebView: KinesteXWebView // Add this
    private lateinit var rootView:ConstraintLayout

    val callback = object : MessageCallback {
        override fun onMessageReceived(message: String) {
            viewModel.handle(message)
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootView = findViewById(R.id.root)
        isLoading = MutableLiveData()

        viewModel = ViewModelProvider(this)[ContentViewModel::class.java]

        viewModel.showWebView.observe(this) {
            if (it.equals(State.ERROR.name)) {
                finish()
            }
        }

        // Create KinesteXWebView instance and inject JavaScript interface
        kinesteXWebView = KinesteXWebView.createWebView(
            this,
            "13c5398cf7a98e3469f6fc8a9a5b2b9d5c8a4814",
            "KinesteX",
            "Shahruh",
            "Fitness",
            "",
            callback
        )

        rootView.addView(kinesteXWebView.webView)

        isLoading.observe(this) { loading ->
            if (loading) {
                // WebView is loading
            } else {
                // WebView finished loading
            }
        }
    }
}
