package com.kinestex.KinesteXb2bkotlin

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.kinestex.webviewlib.KinesteXWebView
import com.kinestex.webviewlib.repository.MessageCallback
import com.kinestex.webviewlib.repository.PlanCategory
import com.kinestex.webviewlib.repository.WorkoutCategory


class MainActivity : AppCompatActivity() {
    private lateinit var isLoading: MutableLiveData<Boolean>
    private lateinit var viewModel: ContentViewModel
    private var kinesteXWebView: KinesteXWebView? = null // Add this
    private lateinit var rootView: ConstraintLayout

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

        viewModel = ViewModelProvider(this)[ContentViewModel::class.java]

        viewModel.showWebView.observe(this) {
            if (it.equals(State.ERROR.name)) {
                finish()
            }
        }

        kinesteXWebView = KinesteXWebView.createWebView(
            this,
            "13c5398cf7a98e3469f6fc8a9a5b2b9d5c8a4814",
            "KinesteX",
            "Shahruh<<",
            PlanCategory.Cardio,
            WorkoutCategory.Fitness,
            callback
        )

        if (kinesteXWebView == null) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        } else {
            rootView.addView(kinesteXWebView!!.webView)
        }

//        isLoading.observe(this) { loading ->
//            if (loading) {
//                // WebView is loading
//            } else {
//                // WebView finished loading
//            }
//        }
    }


}
