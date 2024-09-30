package com.colleagues.austrom

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.User

class AustromApplication : Application() {
    private lateinit var sharedPreferences: SharedPreferences

    companion object{
        var appUser : User? = null
        var activeAssets : MutableMap<String, Asset> = mutableMapOf()
        var activeCurrencies : MutableMap<String, Currency> = mutableMapOf()

    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        sharedPreferences = getSharedPreferences("AustromPreferences", Context.MODE_PRIVATE)
    }

    fun setRememberedUser(newUserId: String) {
        val editor = sharedPreferences.edit()
        editor.putString("appUserId",newUserId)
        editor.apply()
    }

    fun getRememberedUser() : String? {
        return sharedPreferences.getString("appUserId", null)
    }

    fun forgetRememberedUser() {
        val editor = sharedPreferences.edit()
        editor.remove("appUserId")
        editor.apply()
    }

    fun setRememberedPin(newPin: String) {
        val editor = sharedPreferences.edit()
        editor.putString("appQuickPin",newPin)
        editor.apply()
    }

    fun getRememberedPin() : String? {
        return sharedPreferences.getString("appQuickPin", null)
    }

    fun setNewBaseCurrency(currency: Currency) {
        if (appUser!=null) {
            val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(null)
            appUser!!.baseCurrencyCode = currency.code
            dbProvider.updateUser(appUser!!)
            Currency.switchRatesToNewBaseCurrency(activeCurrencies, currency.code)
        }
    }

    @SuppressLint("MutatingSharedPrefs")
    fun setRememberedTarget(newTarget: String) {
        val existingTargets = sharedPreferences.getStringSet("targetList", null) ?: mutableSetOf()
        if (!existingTargets.contains(newTarget)) {
            val editor = sharedPreferences.edit()
            existingTargets.add(newTarget)
            editor.putStringSet("targetList",existingTargets)
            editor.apply()
        }
    }

    fun getRememberedTargets(): List<String> {
        return sharedPreferences.getStringSet("targetList", null)?.toList() ?: listOf()
    }
}

