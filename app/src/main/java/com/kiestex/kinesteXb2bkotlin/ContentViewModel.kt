package com.kiestex.kinesteXb2bkotlin


/*
 * Created by shagi on 22.02.2024 22:27
 */

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContentViewModel : ViewModel() {
    val showWebView: MutableLiveData<Boolean> = MutableLiveData(false)
    
    var apiKey: String = "YOUR API KEY"
    var companyName: String = "YOUR COMPANY NAME"
    var userId: String = "YOUR USER ID"
    var planC: String = "Cardio"
    var category: String = "Fitness"

    var message: MutableLiveData<String> = MutableLiveData("")
    var workoutData: MutableLiveData<String> = MutableLiveData("")



    fun handle(message: String) {
        val currentTime = getCurrentTime()
        try {
            val json = JSONObject(message)
            when (json.getString("type")) {
                "finished_workout" -> workoutData.postValue("\nWorkout finished, data received: ${json.getString("data")} @$currentTime")
                "error_occured" -> workoutData.postValue("\nThere was an error: ${json.getString("data")} @$currentTime")
                "exercise_completed" -> workoutData.postValue("\nExercise completed: ${json.getString("data")} @$currentTime")
                "exitApp" -> {
                    showWebView.postValue(false)
                    workoutData.postValue("\nUser closed workout window @$currentTime")
                }
                else -> { }
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
}