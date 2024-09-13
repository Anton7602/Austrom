package com.colleagues.austrom.database

import androidx.fragment.app.FragmentActivity
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.User

interface IDatabaseProvider {
    fun writeNewUser(user: User)
    fun getUserByUsername(username: String, activity: FragmentActivity? = null) : User?

    fun writeNewAsset(asset: Asset)
    fun getAssetsOfUser(user: User, activity: FragmentActivity? = null) : MutableList<Asset>?

    fun writeNewBudget(budget: Budget)
    fun getBudgetById(budgetId: String, activity: FragmentActivity? = null) : Budget?

    fun writeNewTransaction(transaction: Transaction)
    fun getTransactionsOfUser(user: User, activity: FragmentActivity? = null) : MutableList<Transaction>?
}