package com.colleagues.austrom

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.User

class AustromApplication : Application() {
    var appUser : User? = null
    var activeAssets : MutableList<Asset> = mutableListOf()

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

