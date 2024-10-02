package com.colleagues.austrom.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var userId: String? = null,
    var username: String? = null,
    var email: String? = null,
    val password: String? = null,
    var activeBudgetId: String? = null,
    var primaryPaymentMethod: String? = null,
    var baseCurrencyCode: String = "USD")
{

}