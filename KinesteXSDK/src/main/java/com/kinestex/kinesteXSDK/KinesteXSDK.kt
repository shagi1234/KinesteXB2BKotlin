package com.kinestex.kinesteXSDK

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class WebViewState : ViewModel() {
    val webView: MutableLiveData<WebView?> = MutableLiveData(null)
}

sealed class WebViewMessage {
    data class KinestexLaunched(val data: Map<String, Any>) : WebViewMessage()
    data class FinishedWorkout(val data: Map<String, Any>) : WebViewMessage()
    data class ErrorOccurred(val data: Map<String, Any>) : WebViewMessage()
    data class ExerciseCompleted(val data: Map<String, Any>) : WebViewMessage()
    data class ExitKinestex(val data: Map<String, Any>) : WebViewMessage()
    data class WorkoutOpened(val data: Map<String, Any>) : WebViewMessage()
    data class WorkoutStarted(val data: Map<String, Any>) : WebViewMessage()
    data class PlanUnlocked(val data: Map<String, Any>) : WebViewMessage()
    data class CustomType(val data: Map<String, Any>) : WebViewMessage()
    data class Reps(val data: Map<String, Any>) : WebViewMessage()
    data class Mistake(val data: Map<String, Any>) : WebViewMessage()
    data class LeftCameraFrame(val data: Map<String, Any>) : WebViewMessage()
    data class ReturnedCameraFrame(val data: Map<String, Any>) : WebViewMessage()
    data class WorkoutOverview(val data: Map<String, Any>) : WebViewMessage()
    data class ExerciseOverview(val data: Map<String, Any>) : WebViewMessage()
    data class WorkoutCompleted(val data: Map<String, Any>) : WebViewMessage()

}

enum class Gender {
    MALE, FEMALE, UNKNOWN
}

enum class Lifestyle {
    SEDENTARY, SLIGHTLY_ACTIVE, ACTIVE, VERY_ACTIVE
}

data class UserDetails(
    val age: Int,
    val height: Int,
    val weight: Int,
    val gender: Gender,
    val lifestyle: Lifestyle
)

sealed class PlanCategory {
    object Cardio : PlanCategory()
    object WeightManagement : PlanCategory()
    object Strength : PlanCategory()
    object Rehabilitation : PlanCategory()
    data class Custom(val name: String) : PlanCategory()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlanCategory) return false

        return when (this) {
            is Cardio -> other is Cardio
            is WeightManagement -> other is WeightManagement
            is Strength -> other is Strength
            is Rehabilitation -> other is Rehabilitation
            is Custom -> other is Custom && this.name == other.name
        }
    }

    override fun hashCode(): Int {
        return when (this) {
            is Cardio -> Cardio::class.hashCode()
            is WeightManagement -> WeightManagement::class.hashCode()
            is Strength -> Strength::class.hashCode()
            is Rehabilitation -> Rehabilitation::class.hashCode()
            is Custom -> name.hashCode()
        }
    }

}

class KinesteXSDK(
    private val context: Context,
) {
    private var planCatString: String? = null

    private fun validateInput(
        planCategory: PlanCategory,
    ): String? {

        when (planCategory) {
            is PlanCategory.Cardio -> this.planCatString = "Cardio"
            is PlanCategory.WeightManagement -> this.planCatString = "Weight Management"
            is PlanCategory.Strength -> this.planCatString = "Strength"
            is PlanCategory.Rehabilitation -> this.planCatString = "Rehabilitation"
            is PlanCategory.Custom -> {
                if (planCategory.name.isEmpty()) {
                    return "planCategory cannot be empty"
                } else if (containsDisallowedCharacters(planCategory.name)) {
                    return "planCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
                }

                this.planCatString = planCategory.name
            }
        }

        return null
    }

    @SuppressLint("ViewConstructor")
    class GenericWebView(
        context: Context,
        apiKey: String,
        companyName: String,
        userId: String,
        url: String,
        onMessageReceived: (WebViewMessage) -> Unit,
        isLoading: MutableLiveData<Boolean>,
        data: Map<String, Any>
    ) : WebView(context) {
        private var viewModel: WebViewState = WebViewState()

        init {
            createGenericWebView(
                this,
                apiKey,
                companyName,
                userId,
                url,
                isLoading,
                data,
                onMessageReceived
            )
        }

        @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
        private fun createGenericWebView(
            webView: WebView,
            apiKey: String,
            companyName: String,
            userId: String,
            url: String,
            isLoading: MutableLiveData<Boolean>,
            data: Map<String, Any>,
            messageCallback: (WebViewMessage) -> Unit
        ) {
            viewModel.webView.value = webView

            with(webView) {
                webChromeClient = WebChromeClient()

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isLoading.value = false

                        postMessage(
                            view,
                            apiKey,
                            companyName,
                            userId,
                            data,
                            url
                        )
                    }
                }

                settings.javaScriptEnabled = true
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun postMessage(message: String) {
                        handleMessage(message, messageCallback)
                    }
                }, "messageHandler")

                loadUrl(url)
            }
        }

        private fun handleMessage(message: String, messageCallback: (WebViewMessage) -> Unit) {
            try {
                val json = JSONObject(message)
                val type = json.optString("type")
                val data = json.optJSONObject("data") ?: JSONObject()

                val dataMap = mutableMapOf<String, Any>()
                data.keys().forEach { key ->
                    dataMap[key] = data[key]
                }

                val webViewMessage = when (type) {
                    "kinestex_launched" -> WebViewMessage.KinestexLaunched(dataMap)
                    "finished_workout" -> WebViewMessage.FinishedWorkout(dataMap)
                    "error_occurred" -> WebViewMessage.ErrorOccurred(dataMap)
                    "exercise_completed" -> WebViewMessage.ExerciseCompleted(dataMap)
                    "exit_kinestex" -> WebViewMessage.ExitKinestex(dataMap)
                    "workout_opened" -> WebViewMessage.WorkoutOpened(dataMap)
                    "workout_started" -> WebViewMessage.WorkoutStarted(dataMap)
                    "plan_unlocked" -> WebViewMessage.PlanUnlocked(dataMap)
                    "mistake" -> WebViewMessage.Mistake(dataMap)
                    "successful_repeat" -> WebViewMessage.Reps(dataMap)
                    "left_camera_frame" -> WebViewMessage.LeftCameraFrame(dataMap)
                    "returned_camera_frame" -> WebViewMessage.ReturnedCameraFrame(dataMap)
                    "workout_overview" -> WebViewMessage.WorkoutOverview(dataMap)
                    "exercise_overview" -> WebViewMessage.ExerciseOverview(dataMap)
                    "workout_completed" -> WebViewMessage.WorkoutCompleted(dataMap)
                    else -> WebViewMessage.CustomType(dataMap)
                }

                // Invoke the callback with the parsed message
                messageCallback(webViewMessage)
            } catch (e: JSONException) {
                Log.e("WebView", "Error parsing JSON message: $message", e)
            }
        }

        fun postMessage(
            webView: WebView?,
            apiKey: String?,
            companyName: String,
            userId: String,
            data: Map<String, Any?>,
            url: String?
        ) {
            val script = buildString {
                append("window.postMessage({")
                append("'key': '${apiKey}', ")
                append("'company': '${companyName}', ")
                append("'userId': '${userId}', ")
                append("'exercises': ${jsonString(data["exercises"] as? List<String> ?: emptyList())}, ")
                append("'currentExercise': '${data["currentExercise"] as? String ?: ""}'")

                data.forEach { (key, value) ->
                    if (key != "exercises" && key != "currentExercise") {
                        append(", '$key': '$value'")
                    }
                }
                append("}, '${url}');")
            }

            webView?.evaluateJavascript(script) { result ->
                if (result != null) {
                    Log.d("WebView", "Result: $result")
                }
            }
        }

        private fun jsonString(from: List<String>): String {
            return try {
                val jsonArray = JSONArray(from)
                jsonArray.toString()
            } catch (e: JSONException) {
                "[]"
            }
        }

        fun updateCurrentExercise(exercise: String) {
            val webView = viewModel.webView ?: run {
                Log.e("WebViewManager", "⚠️ WebView is not available")
                return
            }

            val script = """
        window.postMessage({ 'currentExercise': '$exercise' }, '*');
    """.trimIndent()

            webView.value?.evaluateJavascript(script) { result ->
                Log.d("WebViewManager", "✅ Successfully sent an update: $result")
            }
        }
    }


    companion object {

        private var cameraWebView: GenericWebView? = null

        private fun validateInput(
            apiKey: String,
            companyName: String,
            userId: String,
            planCategory: PlanCategory,
        ): String? {
            // Perform validation checks here
            // Return null if validation is successful, or an error message string if not
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                )
            ) {
                return "apiKey, companyName, or userId contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
            }

            when (planCategory) {

                is PlanCategory.Custom -> {
                    if (planCategory.name.isEmpty()) {
                        return "planCategory cannot be empty"
                    } else if (containsDisallowedCharacters(planCategory.name)) {
                        return "planCategory contains disallowed characters: < >, { }, ( ), [ ], ;, \", ', $, ., #, or <script>"
                    }
                }

                else -> {
                    return null
                }
            }

            return null
        }


        private fun containsDisallowedCharacters(input: String): Boolean {
            val disallowedCharacters = setOf(
                '<',
                '>',
                '{',
                '}',
                '(',
                ')',
                '[',
                ']',
                ';',
                '"',
                '\'',
                '$',
                '.',
                '#',
                '<',
                '>'
            )
            return input.any { it in disallowedCharacters }
        }

        private fun planCategoryString(category: PlanCategory): String {
            return when (category) {
                PlanCategory.Cardio -> "Cardio"
                PlanCategory.WeightManagement -> "Weight Management"
                PlanCategory.Strength -> "Strength"
                PlanCategory.Rehabilitation -> "Rehabilitation"
                is PlanCategory.Custom -> category.name
            }
        }

        private fun genderString(gender: Gender): String {
            return when (gender) {
                Gender.MALE -> "Male"
                Gender.FEMALE -> "Female"
                Gender.UNKNOWN -> "Male"
            }
        }

        private fun lifestyleString(lifestyle: Lifestyle): String {
            return when (lifestyle) {
                Lifestyle.SEDENTARY -> "Sedentary"
                Lifestyle.SLIGHTLY_ACTIVE -> "Slightly Active"
                Lifestyle.ACTIVE -> "Active"
                Lifestyle.VERY_ACTIVE -> "Very Active"
            }
        }

        fun createMainView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            planCategory: PlanCategory = PlanCategory.Cardio,
            user: UserDetails?,
            isLoading: MutableLiveData<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit
        ): WebView? {
            val validationError = validateInput(apiKey, companyName, userId, planCategory)

            if (validationError != null) {
                Log.e("WebViewManager", "⚠️ Validation Error: $validationError")
                return null
            } else {
                val data = mutableMapOf<String, Any>(
                    "planC" to planCategoryString(planCategory)
                )
                user?.let {
                    data["age"] = it.age
                    data["height"] = it.height
                    data["weight"] = it.weight
                    data["gender"] = genderString(it.gender)
                    data["lifestyle"] = lifestyleString(it.lifestyle)
                }

                return GenericWebView(
                    context = context,
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = "https://kinestex-plans-git-feature-route-greatest-team.vercel.app",
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived
                )
            }
        }

        /**
        Creates a view for a specific workout plan. Keeps track of the progress for that particular plan, recommending the workouts according to the person's progression

        - Parameters:
        - apiKey: The API key for authentication.
        - companyName: The name of the company using the framework provided by KinesteX
        - userId: The unique identifier for the user.
        - planName: The name of the workout plan.
        - user: Optional user details including age, height, weight, gender, and lifestyle.
        - isLoading: A binding to a Boolean value indicating if the view is loading.
        - onMessageReceived: A closure that handles messages received from the WebView.
         */

        fun createPlanView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            planName: String,
            user: UserDetails?,
            isLoading: MutableLiveData<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit
        ): WebView? {
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                ) || containsDisallowedCharacters(planName)
            ) {
                Log.e(
                    "WebViewManager",
                    "⚠️ Validation Error: apiKey, companyName, userId, or planName contains disallowed characters"
                )
                return null
            } else {
                val adjustedPlanName = planName.replace(" ", "%20")
                val url =
                    "https://kinestex-plans-git-feature-route-greatest-team.vercel.app/plan/$adjustedPlanName"
                val data = mutableMapOf<String, Any>()
                user?.let {
                    data["age"] = it.age
                    data["height"] = it.height
                    data["weight"] = it.weight
                    data["gender"] = genderString(it.gender)
                    data["lifestyle"] = lifestyleString(it.lifestyle)
                }
                return GenericWebView(
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = url,
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived,
                    context = context
                )
            }
        }

        /**
        Creates a view for a specific workout.

        - Parameters:
        - apiKey: The API key for authentication.
        - companyName: The name of the company using the framework.
        - userId: The unique identifier for the user.
        - workoutName: The name of the workout.
        - user: Optional user details including age, height, weight, gender, and lifestyle.
        - isLoading: A binding to a Boolean value indicating if the view is loading.
        - onMessageReceived: A closure that handles messages received from the WebView.
         */

        fun createWorkoutView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            workoutName: String,
            user: UserDetails?,
            isLoading: MutableLiveData<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit
        ): WebView? {
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                ) || containsDisallowedCharacters(workoutName)
            ) {
                Log.e(
                    "WebViewManager",
                    "Validation Error: apiKey, companyName, userId, or workoutName contains disallowed characters"
                )
                return null
            } else {
                val adjustedWorkoutName = workoutName.replace(" ", "%20")
                val url =
                    "https://kinestex-plans-git-feature-route-greatest-team.vercel.app/workout/$adjustedWorkoutName"
                val data: Map<String, Any> = mapOf(
                    "age" to (user?.age ?: ""),
                    "height" to (user?.height ?: ""),
                    "weight" to (user?.weight ?: ""),
                    "gender" to (user?.gender?.let { genderString(it) } ?: ""),
                    "lifestyle" to (user?.lifestyle?.let { lifestyleString(it) } ?: "")
                )

                return GenericWebView(
                    context = context,
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = url,
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived
                )
            }
        }

        /**
        Creates a view for a specific exercise challenge.

        - Parameters:
        - apiKey: The API key for authentication.
        - companyName: The name of the company using the framework.
        - userId: The unique identifier for the user.
        - exercise: The name of the exercise (default is "Squats").
        - countdown: The countdown time for the challenge.
        - user: Optional user details including age, height, weight, gender, and lifestyle.
        - isLoading: A binding to a Boolean value indicating if the view is loading.
        - onMessageReceived: A closure that handles messages received from the WebView.
         */

        fun createChallengeView(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            exercise: String,
            countdown: Int,
            user: UserDetails?,
            isLoading: MutableLiveData<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit
        ): WebView? {
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                ) || containsDisallowedCharacters(exercise)
            ) {
                Log.e(
                    "WebViewManager",
                    "Validation Error: apiKey, companyName, userId, or exercise contains disallowed characters"
                )
                return null
            } else {
                val data: Map<String, Any> = mapOf(
                    "exercise" to exercise,
                    "countdown" to countdown,
                    "age" to (user?.age ?: ""),
                    "height" to (user?.height ?: ""),
                    "weight" to (user?.weight ?: ""),
                    "gender" to (user?.gender?.let { genderString(it) } ?: ""),
                    "lifestyle" to (user?.lifestyle?.let { lifestyleString(it) } ?: "")
                )

                return GenericWebView(
                    context = context,
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = "https://kinestex-challenge.vercel.app",
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived
                )
            }
        }

        fun createCameraComponent(
            context: Context,
            apiKey: String,
            companyName: String,
            userId: String,
            exercises: List<String>,
            currentExercise: String,
            user: UserDetails?,
            isLoading: MutableLiveData<Boolean>,
            onMessageReceived: (WebViewMessage) -> Unit
        ): WebView? {
            for (exercise in exercises) {
                if (containsDisallowedCharacters(exercise)) {
                    Log.e(
                        "WebViewManager",
                        "Validation Error: $exercise contains disallowed characters"
                    )
                    return null
                }
            }
            if (containsDisallowedCharacters(apiKey) || containsDisallowedCharacters(companyName) || containsDisallowedCharacters(
                    userId
                ) || containsDisallowedCharacters(currentExercise)
            ) {
                Log.e(
                    "WebViewManager",
                    "Validation Error: apiKey, companyName, userId, or currentExercise contains disallowed characters"
                )
                return null
            } else {
                val data = mapOf(
                    "exercises" to exercises,
                    "currentExercise" to currentExercise,
                    "age" to (user?.age ?: ""),
                    "height" to (user?.height ?: ""),
                    "weight" to (user?.weight ?: ""),
                    "gender" to (user?.gender?.let { genderString(it) } ?: ""),
                    "lifestyle" to (user?.lifestyle?.let { lifestyleString(it) } ?: "")
                )

                val cameraWebViewInstance = GenericWebView(
                    context = context,
                    apiKey = apiKey,
                    companyName = companyName,
                    userId = userId,
                    url = "https://kinestex-camera-ai.vercel.app",
                    data = data,
                    isLoading = isLoading,
                    onMessageReceived = onMessageReceived
                )

                cameraWebView = cameraWebViewInstance
                return cameraWebViewInstance
            }
        }

        /**
         * Updates the current exercise in the camera component.
         *
         * @param exercise The name of the current exercise.
         */
        fun updateCurrentExercise(exercise: String) {
            cameraWebView?.updateCurrentExercise(exercise)
        }

    }
}
