package com.kinestex.kinesteXb2bKotlin


/*
 * Created by shagi on 22.02.2024 22:27
 */

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kinestex.kinesteXb2bKotlin.data.IntegrationOption
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContentViewModel : ViewModel() {
    val showWebView: MutableStateFlow<WebViewState> = MutableStateFlow(WebViewState.LOADING)

    val reps: MutableStateFlow<Int> = MutableStateFlow(0)
    val mistake: MutableStateFlow<String> = MutableStateFlow("")

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    var selectedOptionPosition: MutableStateFlow<Int> = MutableStateFlow(0)

    var selectedSubOption: Int = 0
    var integrateOptions: List<IntegrationOption> = generateOptions()

    private fun generateOptions(): List<IntegrationOption> {

        val completeUX = IntegrationOption(
            "Conplete UX",
            "Goal Category",
            mutableListOf(
                "Cardio",
                "Strength",
                "Rehabilitation",
                "WeightManagement"
            )
        )
        val workoutPlan = IntegrationOption(
            "Workout Plan",
            "Plan",
            mutableListOf(
                "Full Cardio",
                "Elastic Evolution",
                "Circuit Training",
                "Fitness Cardio"
            )
        )
        val workout = IntegrationOption(
            "Workout",
            "Workout",
            mutableListOf(
                "Fitness Lite",
                "Circuit Training",
                "Tabata"
            )
        )

        val challenge = IntegrationOption(
            "Challenge",
            "Challenge",
            mutableListOf(
                "Squats",
                "Jumping Jack"
            )
        )
        val camera = IntegrationOption(
            title = "Camera"
        )

        return mutableListOf(completeUX, workoutPlan, workout, challenge, camera)
    }

    fun setOption(i: Int) {
        selectedOptionPosition.value = i
    }
}