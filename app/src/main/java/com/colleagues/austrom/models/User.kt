package com.colleagues.austrom.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val activeBudgetID: String? = null) {

}