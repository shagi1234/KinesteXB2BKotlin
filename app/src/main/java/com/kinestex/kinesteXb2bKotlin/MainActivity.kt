package com.kinestex.kinesteXb2bKotlin

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kinestex.kinesteXSDK.KinesteXSDK
import com.kinestex.kinesteXSDK.PlanCategory
import com.kinestex.kinesteXSDK.WebViewMessage
import com.kinestex.kinesteXb2bKotlin.databinding.ActivityMainBinding
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ContentViewModel
    private lateinit var binding: ActivityMainBinding
    private var iconSubOptions: MutableList<ImageView> = mutableListOf()
    private var webView: WebView? = null

    private var tvMistake: TextView? = null
    private var tvReps: TextView? = null


    private val apiKey = "678c2c690b1b2496c20fd42676794da5a77291f6" // store this key securely
    private val company = "CAREVOICE"
    private val userId = "user1"

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[ContentViewModel::class.java]

        initUiListeners()

        observe()

    }

    private fun initUiListeners() {
        binding.toggleOptions.setOnClickListener {
            handleDropDown(binding.iconDropdown, binding.collapsableContent)
        }

        binding.toggleOptionsCategory.setOnClickListener {
            handleDropDown(binding.iconDropdownCategory, binding.collapsableContentCategory)
        }

        binding.next.setOnClickListener {
            handleNextButton()
        }

        binding.completeUx.setOnClickListener {
            unCheckOldPosition()
            viewModel.setOption(0)
            setChecked(binding.iconRadioCompleteUx)
        }
        binding.workoutPlan.setOnClickListener {
            unCheckOldPosition()
            viewModel.setOption(1)
            setChecked(binding.iconRadioWorkoutPlan)
        }
        binding.workout.setOnClickListener {
            unCheckOldPosition()
            viewModel.setOption(2)
            setChecked(binding.iconRadioWorkout)
        }
        binding.challenge.setOnClickListener {
            unCheckOldPosition()
            viewModel.setOption(3)
            setChecked(binding.iconRadioChallenge)
        }
        binding.camera.setOnClickListener {
            unCheckOldPosition()
            viewModel.setOption(4)
            setChecked(binding.iconRadioCamera)
        }


    }

    private fun handleNextButton() {
        val view = createWebView()

        view?.let {
            viewModel.showWebView.value = WebViewState.SUCCESS

            if (viewModel.selectedOptionPosition.value == 4) {
                binding.layoutWebView.addView(view)
            }
        }
    }

    private fun setChecked(iconRadioCompleteUx: ImageView) {
        iconRadioCompleteUx.setImageResource(R.drawable.radio_active)
    }

    private fun unCheckOldPosition() {
        when (viewModel.selectedOptionPosition.value) {
            0 -> {
                binding.iconRadioCompleteUx.setImageResource(R.drawable.radio_unchecked)
            }

            1 -> {
                binding.iconRadioWorkoutPlan.setImageResource(R.drawable.radio_unchecked)
            }

            2 -> {
                binding.iconRadioWorkout.setImageResource(R.drawable.radio_unchecked)
            }

            3 -> {
                binding.iconRadioChallenge.setImageResource(R.drawable.radio_unchecked)
            }

            4 -> {
                binding.iconRadioCamera.setImageResource(R.drawable.radio_unchecked)
            }
        }
    }

    private fun handleDropDown(
        icon: View,
        collapsableContent: LinearLayout
    ) {

        if (icon.rotation == 0f) {
            icon.animate().rotation(180f).duration = 200
            AnimationUtils.expand(collapsableContent, 200)
        } else {
            icon.animate().rotation(0f).duration = 200
            AnimationUtils.collapse(collapsableContent, 200)
        }
    }

    private fun setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    private fun createWebView(): View? {
        var view: View? = null

        val subOption = viewModel.integrateOptions[viewModel.selectedOptionPosition.value].subOption?.get(viewModel.selectedSubOption)

        when (viewModel.selectedOptionPosition.value) {
            0 -> {
                webView = KinesteXSDK.createMainView(
                    this,
                    apiKey = apiKey,
                    companyName = company,
                    userId = userId,
                    planCategory = getPlanCategory(subOption),
                    user = null,
                    isLoading = viewModel.isLoading,
                    onMessageReceived = { message ->
                        when (message) {

                            is WebViewMessage.ExitKinestex -> {
                                lifecycleScope.launch {
                                    viewModel.showWebView.emit(WebViewState.ERROR)
                                }
                            }

                            else -> {

                            }
                        }
                    }
                )

                view = webView

            }

            1 -> {
                webView = KinesteXSDK.createPlanView(
                    this,
                    apiKey = apiKey,
                    companyName = company,
                    userId = userId,
                    planName = subOption ?: "",
                    user = null,
                    isLoading = viewModel.isLoading,
                    onMessageReceived = { message ->
                        when (message) {

                            is WebViewMessage.ExitKinestex -> {
                                lifecycleScope.launch {
                                    viewModel.showWebView.emit(WebViewState.ERROR)
                                }
                            }

                            else -> {

                            }
                        }
                    }
                )

                view = webView

            }

            2 -> {

                webView = KinesteXSDK.createWorkoutView(
                    this,
                    apiKey = apiKey,
                    companyName = company,
                    userId = userId,
                    workoutName = subOption ?: "",
                    user = null,
                    isLoading = viewModel.isLoading,
                    onMessageReceived = { message ->
                        when (message) {

                            is WebViewMessage.ExitKinestex -> {
                                lifecycleScope.launch {
                                    viewModel.showWebView.emit(WebViewState.ERROR)
                                }
                            }

                            else -> {

                            }
                        }
                    }
                )

                view = webView

            }

            3 -> {

                webView = KinesteXSDK.createChallengeView(
                    this,
                    apiKey = apiKey,
                    companyName = company,
                    userId = userId,
                    exercise = subOption ?: "",
                    user = null,
                    countdown = 100,
                    isLoading = viewModel.isLoading,
                    onMessageReceived = { message ->
                        when (message) {

                            is WebViewMessage.ExitKinestex -> {
                                lifecycleScope.launch {
                                    viewModel.showWebView.emit(WebViewState.ERROR)
                                }
                            }

                            else -> {

                            }
                        }
                    }
                )
                view = webView

            }

            4 -> {
                view = createCameraComponentView(this)
            }


        }

        return view
    }

    private fun getPlanCategory(s: String?): PlanCategory {
        return when (s?.lowercase()) {
            "cardio" -> PlanCategory.Cardio
            "weightmanagement" -> PlanCategory.WeightManagement
            "strength" -> PlanCategory.Strength
            "rehabilitation" -> PlanCategory.Rehabilitation
            else -> PlanCategory.Custom(s ?: "")
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModel.showWebView.collect { state ->
                when (state) {
                    WebViewState.LOADING -> {
                        //
                    }

                    WebViewState.ERROR -> {
                        binding.layoutWebView.removeAllViews()
                        binding.layoutWebView.visibility = View.GONE
                    }

                    WebViewState.SUCCESS -> {
                        binding.layoutWebView.visibility = View.VISIBLE

                        webView?.let {
                            if (viewModel.selectedOptionPosition.value == 4) return@collect

                            val view = setLayoutParamsFullScreen(it)
                            binding.layoutWebView.removeAllViews()
                            binding.layoutWebView.addView(view)
                        }

                    }
                }
            }
        }


        lifecycleScope.launch {
            viewModel.selectedOptionPosition.collect {
                binding.next.text = "View ${viewModel.integrateOptions[it].title}"
                createSubOption(it)
            }
        }

        lifecycleScope.launch {
            viewModel.mistake.collect { mistake ->
                tvMistake?.let {
                    it.text = "MISTAKE: $mistake"
                }
            }
        }

        lifecycleScope.launch {
            viewModel.reps.collect { reps ->
                tvReps?.let {
                    it.text = "REPS: $reps"
                }
            }
        }


    }

    private fun createSubOption(optionPosition: Int) {
        val subOptions = viewModel.integrateOptions[optionPosition].subOption

        if (subOptions.isNullOrEmpty()) {
            binding.layoutCategory.visibility = View.GONE
            return
        }

        binding.layoutCategory.visibility = View.VISIBLE
        binding.collapsableContentCategory.removeAllViews()
        binding.collapsableContentCategory.invalidate()
        iconSubOptions.clear()
        viewModel.selectedSubOption = 0


        subOptions.forEachIndexed { index, title ->
            val optionView = createOptionView(this, title, index)

            optionView.setOnClickListener {
                val oldPosition = viewModel.selectedSubOption

                try {
                    iconSubOptions[oldPosition].setImageResource(R.drawable.radio_unchecked)
                    viewModel.selectedSubOption = index
                    iconSubOptions[index].setImageResource(R.drawable.radio_active)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            binding.collapsableContentCategory.addView(optionView)
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        binding.collapsableContentCategory.layoutParams = params

    }

    private fun createCameraComponentView(
        context: Context
    ): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setPadding(16, 16, 16, 16)
        }

        val repsTextView = TextView(context).apply {
            text = "REPS: 0"
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        val mistakeTextView = TextView(context).apply {
            text = "MISTAKE: --"
            setTextColor(context.resources.getColor(android.R.color.holo_red_dark, null))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        container.addView(repsTextView)
        container.addView(mistakeTextView)

        webView = KinesteXSDK.createCameraComponent(
            context = context,
            apiKey = apiKey,
            companyName = company,
            userId = userId,
            currentExercise = "Squats",
            exercises = listOf("Squats"),
            user = null,
            isLoading = viewModel.isLoading,
            onMessageReceived = { message ->
                when (message) {
                    is WebViewMessage.ExitKinestex -> {
                        lifecycleScope.launch {
                            viewModel.showWebView.emit(WebViewState.ERROR)
                        }
                    }

                    is WebViewMessage.Reps -> {
                        val reps = message.data["value"] as? Int ?: 0
                        lifecycleScope.launch {
                            viewModel.reps.emit(reps)
                        }

                    }

                    is WebViewMessage.Mistake -> {
                        val mistake = message.data["value"] as? String ?: "--"
                        lifecycleScope.launch {
                            viewModel.mistake.emit(mistake)
                        }

                    }

                    else -> {
                        // Handle other messages
                    }
                }
            }
        )

        webView?.let {
            container.addView(setLayoutParamsFullScreen(it))
        }

        return container
    }

    private fun setLayoutParamsFullScreen(view: View): View {
        view.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        return view
    }


    private fun createOptionView(
        context: Context,
        title: String,
        index: Int,
    ): LinearLayout {
        val linearLayout = LinearLayout(context)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.layoutParams = params
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.setPadding(
            dpToPx(context, 10),
            dpToPx(context, 7),
            dpToPx(context, 10),
            dpToPx(context, 7)
        )

        // Create TextView
        val textView = TextView(context)
        val textParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
        textView.layoutParams = textParams
        textView.text = title
        textView.setTextColor(Color.WHITE)

        // Create ImageView
        val imageView = ImageView(context)

        val imageParams = LinearLayout.LayoutParams(
            dpToPx(context, 20),
            dpToPx(context, 20)
        )

        imageView.layoutParams = imageParams
        imageView.setImageResource(if (index == 0) R.drawable.radio_active else R.drawable.radio_unchecked)

        // Add views to LinearLayout
        iconSubOptions.add(imageView)

        linearLayout.addView(textView)
        linearLayout.addView(imageView)

        return linearLayout
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onBackPressed() {

        webView?.let {
            if (it.canGoBack()) it.goBack()
            else {
                if (viewModel.showWebView.value == WebViewState.ERROR) {
                    super.onBackPressed()
                } else {
                    lifecycleScope.launch {
                        viewModel.showWebView.emit(WebViewState.ERROR)
                    }

                }
            }
            return
        }
        if (viewModel.showWebView.value == WebViewState.ERROR) {
            super.onBackPressed()
        } else {
            lifecycleScope.launch {
                viewModel.showWebView.emit(WebViewState.ERROR)
            }
        }


    }

}


