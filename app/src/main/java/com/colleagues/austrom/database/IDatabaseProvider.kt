package com.colleagues.austrom.database

import androidx.appcompat.app.AppCompatActivity
import com.colleagues.austrom.models.User

interface IDatabaseProvider {
    fun writeNewUser(user: User)
    fun getUserByUsername(username: String, activity: AppCompatActivity? = null) : User?
}