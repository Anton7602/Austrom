package com.colleagues.austrom.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var userId: String? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val activeBudgetId: String? = null) {

}