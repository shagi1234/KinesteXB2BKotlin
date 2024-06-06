package com.kinestex.kinesteXb2bKotlin


/*
 * Created by shagi on 22.02.2024 22:27
 */

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kinestex.kinesteXb2bKotlin.data.IntegrationOption
import com.kinestex.kinesteXb2bKotlin.data.IntegrationOptionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
        return IntegrationOptionType.entries.map { optionType ->
            IntegrationOption(
                title = optionType.title,
                optionType = optionType.category,
                subOption = optionType.subOptions?.toMutableList()
            )
        }
    }

    fun setOption(i: Int) {
        selectedOptionPosition.value = i
    }

    fun setMistake(it: String) {
        viewModelScope.launch {
            mistake.emit(it)
        }
    }

    fun setReps(it: Int) {
        viewModelScope.launch {
            reps.emit(it)
        }
    }
}