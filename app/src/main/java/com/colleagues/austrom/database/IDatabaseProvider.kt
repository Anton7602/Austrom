package com.colleagues.austrom.database

import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.User

interface IDatabaseProvider {
    fun createNewUser(user: User)
    fun getUserByUserId(userId: String): User?
    fun getUserByUsername(username: String) : User?


    fun createNewAsset(asset: Asset)
    fun getAssetsOfUser(user: User) : MutableList<Asset>?

    fun createNewBudget(budget: Budget, budgetCreator: User)
    fun getBudgetById(budgetId: String) : Budget?
    fun addUserToBudget(budget: Budget, user: User)
    fun removeUserFromBudget(budget: Budget, user: User)

    fun writeNewTransaction(transaction: Transaction)
    fun getTransactionsOfUser(user: User) : MutableList<Transaction>?
}