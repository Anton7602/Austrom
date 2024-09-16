package com.colleagues.austrom.database

import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.User

interface IDatabaseProvider {
    fun createNewUser(user: User): String?
    fun updateUser(user: User)
    fun deleteUser(user: User)
    fun getUserByUserId(userId: String): User?
    fun getUserByUsername(username: String) : User?

    fun createNewAsset(asset: Asset): String?
    fun updateAsset(asset: Asset)
    fun deleteAsset(asset: Asset)
    fun getAssetsOfUser(user: User) : MutableMap<String, Asset>
    fun getAssetsOfBudget(budget: Budget) : MutableMap<String, Asset>

    fun createNewBudget(budget: Budget): String?
    fun updateBudget(budget: Budget)
    fun deleteBudget(budget: Budget)
    fun getBudgetById(budgetId: String) : Budget?

    fun writeNewTransaction(transaction: Transaction): String?
    fun getTransactionsOfUser(user: User) : MutableList<Transaction>

    fun getCurrencies(): MutableMap<String, Currency>
}