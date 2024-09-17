package com.colleagues.austrom

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.User

class AustromApplication : Application() {
    companion object{
        var appUser : User? = null
        var activeAssets : MutableMap<String, Asset> = mutableMapOf()
        var activeCurrencies : MutableMap<String, Currency> = mutableMapOf()
        var activeTransactions: MutableList<Transaction> = mutableListOf()
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

