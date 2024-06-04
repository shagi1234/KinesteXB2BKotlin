package com.kinestex.kinesteXb2bKotlin


/*
 * Created by shagi on 22.02.2024 22:27
 */

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kinestex.kinesteXb2bKotlin.data.IntegrationOption
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContentViewModel : ViewModel() {
    val showWebView: MutableLiveData<WebViewState> = MutableLiveData(WebViewState.LOADING)
    val reps: MutableLiveData<Int> = MutableLiveData(0)
    val mistake: MutableLiveData<String> = MutableLiveData("")
    var message: MutableLiveData<String> = MutableLiveData("")
    val isLoading : MutableLiveData<Boolean> = MutableLiveData(false)
    var workoutData: MutableLiveData<String> = MutableLiveData("")

    var selectedOptionPosition: MutableLiveData<Int> = MutableLiveData(0)
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

    fun handle(message: String) {
        val currentTime = getCurrentTime()
        try {
            val json = JSONObject(message)
            when (json.getString("type")) {
                "finished_workout" -> workoutData.postValue(
                    "\nWorkout finished, data received: ${
                        json.getString(
                            "data"
                        )
                    } @$currentTime"
                )

                "error_occured" -> workoutData.postValue("\nThere was an error: ${json.getString("data")} @$currentTime")
                "exercise_completed" -> workoutData.postValue(
                    "\nExercise completed: ${
                        json.getString(
                            "data"
                        )
                    } @$currentTime"
                )

                "exit_kinestex" -> {

                    showWebView.postValue(WebViewState.ERROR)
                    workoutData.postValue("\nUser closed workout window @$currentTime")
                }

                else -> {}
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            println("Could not parse JSON message from WebView.")
        }
    }

    private fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault())
        return formatter.format(Date())
    }

    fun setOption(i: Int) {
        selectedOptionPosition.value = i
    }
}