package com.colleagues.austrom.database

import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Invitation
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.User

interface IRemoteDatabaseProvider {
    fun createNewUser(user: User)
    fun updateUser(user: User)
    fun deleteUser(user: User)
    fun getUserByUserId(userId: String): User?
    fun getUserByUsername(username: String) : User?
    fun getUserByEmail(email: String) : User?
    fun getUsersByBudget(budgetId: String): MutableMap<String, User>

    fun createNewAsset(asset: Asset, budget: Budget)
    fun updateAsset(asset: Asset, budget: Budget)
    fun deleteAsset(asset: Asset, budget: Budget)
    fun deleteAssetsOfBudget(budget: Budget)
//    fun getAssetsOfUser(user: User) : MutableMap<String, Asset>
//    fun getAssetsOfBudget(budget: Budget) : MutableMap<String, Asset>

    fun createNewBudget(budget: Budget)
    fun updateBudget(budget: Budget)
    fun deleteBudget(budget: Budget)
    fun getBudgetById(budgetId: String) : Budget?

    fun insertTransaction(transaction: Transaction, budget: Budget)
    fun updateTransaction(transaction: Transaction, budget: Budget)
    fun deleteTransaction(transaction: Transaction, budget: Budget)
    fun deleteTransactionsOfBudget(budget: Budget)

    fun insertTransactionDetail(transactionDetail: TransactionDetail, budget: Budget)
    fun deleteTransactionDetailsOfBudget(budget: Budget)

    fun insertCategory(category: Category, budget: Budget)
    fun updateCategory(category: Category, budget: Budget)
    fun deleteCategory(category: Category, budget: Budget)
    fun deleteCategoriesOfBudget(budget: Budget)

    fun sentBudgetInvite(invitation: Invitation)
    fun deleteInvitationToUser(user: User, budget: Budget)
    fun deleteInvitationToUser(userId: String, budgetId: String)
//    fun getTransactionsOfUser(user: User) : MutableList<Transaction>
//    fun getTransactionsOfBudget(budget: Budget) : MutableList<Transaction>
//    fun getTransactionsOfAsset(asset: Asset): MutableList<Transaction>

//    fun getCurrencies(): MutableMap<String, Currency>
}