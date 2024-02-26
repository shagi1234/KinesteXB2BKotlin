package com.kinestex.webviewlib.repository


/*
 * Created by shagi on 27.02.2024 01:07
 */

sealed class PlanCategory {
    object Cardio : PlanCategory()
    object WeightManagement : PlanCategory()
    object Strength : PlanCategory()
    object Rehabilitation : PlanCategory()
    data class Custom(val description: String) : PlanCategory()
}