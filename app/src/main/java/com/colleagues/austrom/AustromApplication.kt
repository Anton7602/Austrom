package com.colleagues.austrom

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.User

class AustromApplication : Application() {
//    var appUser : User? = null
//    var activeAssets : MutableMap<String, Asset> = mutableMapOf()
//    var activeCurrencies : MutableMap<String, Currency> = mutableMapOf()

    companion object{
        var appUser : User? = null
        var activeAssets : MutableMap<String, Asset> = mutableMapOf()
        var activeCurrencies : MutableMap<String, Currency> = mutableMapOf()
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

