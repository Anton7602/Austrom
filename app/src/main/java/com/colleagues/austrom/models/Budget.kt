package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.managers.EncryptionManager
import java.util.UUID
import kotlin.random.Random

class Budget(val budgetName: String, var budgetId: String = generateUniqueBudgetId(), val users: ArrayList<String> = arrayListOf()) {
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

            AustromApplication.activeAssets.values.forEach{ asset -> remoteDBProvider.createNewAsset(asset, budget) }
            val transactions = localDBProvider.getTransactionsOfUser(AustromApplication.appUser!!)
            transactions.forEach { transaction ->
                remoteDBProvider.insertTransaction(transaction, budget)
                localDBProvider.getTransactionDetailsOfTransaction(transaction).forEach { transactionDetail ->
                    remoteDBProvider.insertTransactionDetail(transactionDetail, budget)
                }
            }
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
            remoteDBProvider.deleteTransactionDetailsOfBudget(this)
            remoteDBProvider.deleteTransactionsOfBudget(this)
            remoteDBProvider.deleteAssetsOfBudget(this)
            remoteDBProvider.deleteBudget(this)
        } else {
            remoteDBProvider.updateBudget(this)
        }
    }

    fun leave(remoteDBProvider: IRemoteDatabaseProvider) {
        removeUser(AustromApplication.appUser!!, remoteDBProvider)
    }


    fun inviteUser(user: User, localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider): Invitation {
        val invitation = Invitation(user.userId, AustromApplication.appUser!!.tokenId.toString(), this.budgetId)
        localDBProvider.insertInvitation(invitation)
        remoteDBProvider.sentBudgetInvite(invitation)
        return invitation
    }

    fun addUser(user: User, token: String, localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider) {
        if (this.users.contains(user.userId)) return
        this.users.add(user.userId)
        user.activeBudgetId = this.budgetId
        user.tokenId = token
        remoteDBProvider.updateUser(user)
        remoteDBProvider.updateBudget(this)

        AustromApplication.activeAssets.values.forEach{ asset -> remoteDBProvider.createNewAsset(asset, this) }
        val transactions = localDBProvider.getTransactionsOfUser(AustromApplication.appUser!!)
        transactions.forEach { transaction ->
            remoteDBProvider.insertTransaction(transaction, this)
            localDBProvider.getTransactionDetailsOfTransaction(transaction).forEach { transactionDetail ->
                remoteDBProvider.insertTransactionDetail(transactionDetail, this)
            }
        }
    }
}

@Entity
class Invitation(@PrimaryKey(autoGenerate = false) val userId: String, val token: String, val budgetId: String, val invitationCode: String = generateInviteCode()) {
    companion object {
        private fun generateInviteCode(): String { return Random.nextInt(10000000, 100000000).toString() }
        fun deserialize(serializedInvitation: String): Invitation {
            val dataParts = serializedInvitation.split(",")
            return Invitation(
                userId = AustromApplication.appUser!!.userId,
                token = dataParts[0],
                budgetId = dataParts[1]
            )
        }
    }

    fun serialize(): String { return "${token},${budgetId}"}
}

