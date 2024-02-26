package com.kinestex.webviewlib.repository


/*
 * Created by shagi on 27.02.2024 01:07
 */

sealed class WorkoutCategory {
    object Fitness : WorkoutCategory()
    object Rehabilitation : WorkoutCategory()
    data class Custom(val description: String) : WorkoutCategory()
}