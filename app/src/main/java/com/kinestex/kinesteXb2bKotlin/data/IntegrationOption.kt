package com.kinestex.kinesteXb2bKotlin.data

data class IntegrationOption(
    var title: String,
    var optionType: String? = null,
    var subOption: List<String>? = null
)

