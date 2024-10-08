package com.colleagues.austrom

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.models.User

class AustromApplication : Application() {
    private lateinit var sharedPreferences: SharedPreferences

    companion object{
        var appUser : User? = null
        var activeAssets : MutableMap<String, Asset> = mutableMapOf()
        var activeCurrencies : MutableMap<String, Currency> = mutableMapOf()
        var knownUsers : MutableMap<String, User> = mutableMapOf()
        var selectedTransaction: Transaction? = null
        var selectedAsset: Asset? = null


        fun getActiveExpenseCategories(): List<Category> {return getActiveCategoriesOfType(TransactionType.EXPENSE)}
        fun getActiveTransferCategories(): List<Category> {return getActiveCategoriesOfType(TransactionType.TRANSFER)}
        fun getActiveIncomeCategories(): List<Category> {return getActiveCategoriesOfType(TransactionType.INCOME)}

        private fun getActiveCategoriesOfType(transactionType: TransactionType) : List<Category> {
            val activeUser = appUser ?: return listOf()
            val expenseCategories: MutableList<Category> = mutableListOf()
            if (activeUser.activeBudgetId!=null) {
                for (user in knownUsers) {
                    for (category in user.value.categories) {
                        if (!expenseCategories.contains(category) && category.transactionType==transactionType) {
                            expenseCategories.add(category)
                        }
                    }
                }
            } else {
                for (category in activeUser.categories) {
                    if (!expenseCategories.contains(category) && category.transactionType == transactionType) {
                        expenseCategories.add(category)
                    }
                }
            }
            return expenseCategories
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        sharedPreferences = getSharedPreferences("AustromPreferences", Context.MODE_PRIVATE)
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

