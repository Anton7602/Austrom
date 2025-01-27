package com.colleagues.austrom

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.managers.EncryptionManager
import com.colleagues.austrom.managers.SyncManager
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.User
import java.util.Locale
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AustromApplication : Application() {
    private lateinit var sharedPreferences: SharedPreferences

    companion object{
        var appUser : User? = null
        var activeAssets : MutableMap<String, Asset> = mutableMapOf()
        var activeCurrencies : MutableMap<String, Currency> = mutableMapOf()
        var activeIncomeCategories : MutableMap<String, Category> = mutableMapOf()
        var activeExpenseCategories : MutableMap<String, Category> = mutableMapOf()
        var activeTransferCategories: MutableMap<String, Category> = mutableMapOf()
        var knownUsers : MutableMap<String, User> = mutableMapOf()
        var supportedLanguages: List<Locale> = listOf(Locale("en"), Locale("ru"))
        var isApplicationThemeLight = false
        private var appLanguageCode: String? = null

        fun showKeyboard(activity: Activity, view: View) {
            view.requestFocus()
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }

        fun hideKeyboard(activity: Activity, view: View? = null) {
            val focusedView = view ?: activity.currentFocus
            if (focusedView != null) {
                focusedView.clearFocus()
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
        }

        fun updateBaseContextLocale(context: Context?) : Context? {
            if (context == null) return null
            if (appLanguageCode==null) return context
            val locale = Locale(appLanguageCode ?: "en")
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            return context.createConfigurationContext(config);
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        sharedPreferences = getSharedPreferences("AustromPreferences", Context.MODE_PRIVATE)
        appLanguageCode = sharedPreferences.getString("language", null)
    }

    fun setRememberedUser(newUserId: String) {
        sharedPreferences.edit().putString("appUserId",newUserId).apply()
    }

    fun getRememberedUser() : String? {
        return sharedPreferences.getString("appUserId", null)
    }

    fun forgetRememberedUser() {
        sharedPreferences.edit().remove("appUserId").apply()
    }

    fun setRememberedPin(newPin: String) {
        sharedPreferences.edit().putString("appQuickPin",newPin).apply()
    }

    fun getRememberedPin() : String? {
        return sharedPreferences.getString("appQuickPin", null)
    }

    fun forgetRememberedPin() {
        sharedPreferences.edit().remove("appQuickPin").apply()
    }

    fun setNewBaseCurrency(currency: Currency) {
        if (appUser!=null) {
            val dbProvider = LocalDatabaseProvider(this)
            appUser!!.baseCurrencyCode = currency.code
            dbProvider.updateUser(appUser!!)
            Currency.switchRatesToNewBaseCurrency(activeCurrencies, currency.code)
        }
    }

    fun getRememberedTargets(): List<String> {
        return sharedPreferences.getStringSet("targetList", null)?.toList() ?: listOf()
    }

    fun setApplicationLanguage(languageCode: String) {
        appLanguageCode = languageCode
        sharedPreferences.edit().putString("language",languageCode).apply()
    }

    fun getApplicationLanguage() : String {
        return appLanguageCode ?: "en"
    }
}

