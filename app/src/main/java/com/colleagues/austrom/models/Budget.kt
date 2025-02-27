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
            AustromApplication.activeCategories.values.forEach { category -> remoteDBProvider.insertCategory(category, budget) }
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
            remoteDBProvider.deleteCategoriesOfBudget(this)
            remoteDBProvider.deleteBudget(this)
        } else {
            remoteDBProvider.updateBudget(this)
        }
    }

    fun leave(remoteDBProvider: IRemoteDatabaseProvider) {
        removeUser(AustromApplication.appUser!!, remoteDBProvider)
    }


    fun inviteUser(user: User, localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider, providedEmail: String? = null): Invitation {
        val invitation = Invitation(
            userId = user.userId,
            token = AustromApplication.appUser!!.tokenId.toString(),
            budgetId = this.budgetId,
            providedEmail = providedEmail)
        if (localDBProvider.getInvitationsByUserIdAndBudgetId(user.userId, this.budgetId)!=null) {this.recallInvitationToUser(user, localDBProvider, remoteDBProvider)}
        localDBProvider.insertInvitation(invitation)
        remoteDBProvider.sentBudgetInvite(invitation)
        return invitation
    }

    fun recallInvitationToUser(user: User, localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider) {
        localDBProvider.recallInvitationToBudgetToUser(user.userId, this.budgetId)
        remoteDBProvider.deleteInvitationToUser(user, this)
    }

    fun addUser(user: User, token: String, localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider) {
        if (this.users.contains(user.userId)) return
        this.users.add(user.userId)
        user.activeBudgetId = this.budgetId
        user.tokenId = token
        remoteDBProvider.updateUser(user)
        remoteDBProvider.updateBudget(this)

        mergeCategories(localDBProvider, remoteDBProvider)
        AustromApplication.activeAssets.values.forEach{ asset -> remoteDBProvider.createNewAsset(asset, this) }
        val transactions = localDBProvider.getTransactionsOfUser(AustromApplication.appUser!!)
        transactions.forEach { transaction ->
            remoteDBProvider.insertTransaction(transaction, this)
            localDBProvider.getTransactionDetailsOfTransaction(transaction).forEach { transactionDetail ->
                remoteDBProvider.insertTransactionDetail(transactionDetail, this)
            }
        }
    }


    private fun mergeCategories(localDBProvider: LocalDatabaseProvider, remoteDBProvider: IRemoteDatabaseProvider) {
        AustromApplication.activeCategories.values.forEach { category ->
            if (localDBProvider.getTransactionOfCategory(category).isNotEmpty()) {
                remoteDBProvider.insertCategory(category, this)
            }
            else {
                if (category.transactionType!=TransactionType.TRANSFER) {
                    localDBProvider.deleteCategory(category)
                }
            }
        }
    }
}

@Entity(primaryKeys = ["userId","budgetId"])
class Invitation(val userId: String, val budgetId: String, val token: String,
val providedEmail: String? = null, val invitationCode: String = generateInviteCode()) {
    companion object {
        private fun generateInviteCode(): String { return Random.nextInt(10000000, 100000000).toString() }
        fun deserialize(serializedInvitation: String, budgetId: String): Invitation {
            val dataParts = serializedInvitation.split(",")
            return Invitation(
                token = dataParts[0],
                userId = dataParts[1],
                budgetId = budgetId
            )
        }
    }

    fun serialize(): String { return "${token},${userId}"}
}

