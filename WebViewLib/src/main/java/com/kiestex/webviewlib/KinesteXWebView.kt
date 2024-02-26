package com.kiestex.webviewlib

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kiestex.webviewlib.repository.MessageCallback
import com.kiestex.webviewlib.repository.PlanCategory
import com.kiestex.webviewlib.repository.WorkoutCategory


/*
 * Created by shagi on 25.02.2024 01:56
 */

class KinesteXWebView(
    context: Context,
    private var apiKey: String,
    private var companyName: String,
    private var userId: String,
    private var planCategory: String,
    private var workoutCategory: String,
    private val messageCallback: MessageCallback
) {
    var webView: WebView = WebView(context).apply {
        setupWebView(this)

         validateInput(
             apiKey = apiKey,
             companyName = companyName,
             userId = userId,
             planCategory = planCategory,
             workoutCategory = workoutCategory,
         )
    }

    private fun validateInput(apiKey: String,
                              companyName: String,
                              userId: String,
                              planCategory: String,
                              workoutCategory: String
    ): String? {

        if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(userId)) {
            return "apiKey, companyName, or userId contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
        }


        when (planCategory) {
            PlanCategory.Cardio.name -> {
                this.planCategory = "Cardio"
            }
            PlanCategory.WeightManagement.name -> {
                this.planCategory = "Weight Management"
            }
            PlanCategory.Strength.name -> {
                this.planCategory = "Strength"
            }
            PlanCategory.Rehabilitation.name -> {
                this.planCategory = "Rehabilitation"
            }
            else -> {
                if (planCategory.isEmpty()) {
                    return "planCategory cannot be empty"
                } else if (containsDisallowedCharacters(planCategory)) {
                    return "planCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
                }
                this.planCategory = planCategory
            }
        }

        when (workoutCategory) {
            WorkoutCategory.Fitness.name -> this.workoutCategory = "Fitness"
            WorkoutCategory.Rehabilitation.name -> this.workoutCategory = "Rehabilitation"

            else -> {
                if (workoutCategory.isEmpty()) {
                    return "workoutCategory cannot be empty"
                } else if (containsDisallowedCharacters(workoutCategory)) {
                    return "workoutCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
                }
                this.workoutCategory = workoutCategory
            }
        }
        return null
    }


    private fun containsDisallowedCharacters(input: String): Boolean {
        val disallowedCharacters = setOf('<', '>', '{', '}', '(', ')', '[', ']', ';', '"', '\'', '$', '.', '#', '<', '>')
        return input.any { it in disallowedCharacters }
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
                'key': '$apiKey',
                'company': '$companyName',
                'userId': '$userId',
                'planC': '$planCategory',
                'category': '$workoutCategory'
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
