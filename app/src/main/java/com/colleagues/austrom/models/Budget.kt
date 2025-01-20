package com.colleagues.austrom.models

import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.managers.EncryptionManager
import java.util.UUID

class Budget(
    val budgetName: String,
    var budgetId: String = generateUniqueBudgetId(),
    val users: ArrayList<String> = arrayListOf()
) {

    private constructor(): this("", "", arrayListOf())

    companion object{
        fun createNewBudget(budgetOwner: User, budgetName: String, localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider): Budget {
            val budget = Budget(
                budgetName = budgetName,
                budgetId = generateUniqueBudgetId(),
                users = arrayListOf(budgetOwner.userId)
            )
            val encryptionManager = EncryptionManager()
            val budgetEncryptionKey = encryptionManager.generateEncryptionKey()
            budgetOwner.activeBudgetId = budget.budgetId
            budgetOwner.tokenId = encryptionManager.convertSecretKeyToString(budgetEncryptionKey)
            localDBProvider.updateUser(budgetOwner)
            remoteDBProvider.updateUser(budgetOwner)
            remoteDBProvider.createNewBudget(budget)
            return budget
        }

        private fun generateUniqueBudgetId() : String { return UUID.randomUUID().toString() }
    }

    fun removeUser(user: User, remoteDBProvider: IRemoteDatabaseProvider) {
        this.users.remove(user.userId)
        user.activeBudgetId = null
        user.tokenId = null
        remoteDBProvider.updateUser(user)
        if (this.users.isEmpty()) {
            remoteDBProvider.deleteBudget(this)
        } else {
            remoteDBProvider.updateBudget(this)
        }
    }

    fun leave(remoteDBProvider: IRemoteDatabaseProvider) {
        removeUser(AustromApplication.appUser!!, remoteDBProvider)
    }

    fun addUser(user: User, token: String, remoteDBProvider: IRemoteDatabaseProvider) {
        this.users.add(user.userId)
        user.activeBudgetId = this.budgetId
        user.tokenId = token
        remoteDBProvider.updateUser(user)
        remoteDBProvider.updateBudget(this)
    }
}

