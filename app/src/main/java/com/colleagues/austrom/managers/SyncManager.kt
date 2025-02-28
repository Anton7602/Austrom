package com.colleagues.austrom.managers

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.AustromApplication.Companion.activeCategories
import com.colleagues.austrom.AustromApplication.Companion.appUser
import com.colleagues.austrom.AustromApplication.Companion.knownUsers
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.intAfterLastPipe
import com.colleagues.austrom.extensions.substringBeforeLastPipe
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Invitation
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.models.User
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncManager(val context: Context, private val localDBProvider: LocalDatabaseProvider, private val remoteDBProvider: IRemoteDatabaseProvider) {
    private val budget = remoteDBProvider.getBudgetById(appUser?.activeBudgetId.toString())

    fun sync() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                syncAll()
            } catch (e: Exception) {
                Log.e("SyncError", "Error syncing data: ${e.message}")
            }
        }
    }

    private suspend fun syncAll() {
        syncCurrenciesRemoteToLocal()
        syncReceivedInvitations()
        syncUsersRemoteToLocal()
        syncAssetsRemoteToLocal()
        syncCategoriesRemoteToLocal()
        syncTransactionsRemoteToLocal()
        syncTransactionsDetailsRemoteToLocal()
        syncSentBudgetInvitations()
    }

    private suspend fun syncCurrenciesRemoteToLocal() { if (remoteDBProvider is FirebaseDatabaseProvider) setCurrenciesListener(remoteDBProvider) }
    private suspend fun syncReceivedInvitations() {if (remoteDBProvider is FirebaseDatabaseProvider) setReceivedInvitationListener(remoteDBProvider) }
    private suspend fun syncUsersRemoteToLocal() { if (remoteDBProvider is FirebaseDatabaseProvider) setUsersListener(remoteDBProvider) }
    private suspend fun syncAssetsRemoteToLocal() { if (remoteDBProvider is FirebaseDatabaseProvider) setAssetListener(remoteDBProvider) }
    private suspend fun syncCategoriesRemoteToLocal() { if (remoteDBProvider is FirebaseDatabaseProvider) setCategoriesListener(remoteDBProvider) }
    private suspend fun syncTransactionsRemoteToLocal() { if (remoteDBProvider is FirebaseDatabaseProvider) setTransactionListener(remoteDBProvider) }
    private suspend fun syncTransactionsDetailsRemoteToLocal() { if (remoteDBProvider is FirebaseDatabaseProvider) setTransactionDetailListener(remoteDBProvider) }
    private fun syncSentBudgetInvitations() {if (remoteDBProvider is FirebaseDatabaseProvider)  setSentInvitationsListener(remoteDBProvider) }

    private suspend fun setCurrenciesListener(firebaseDatabaseProvider: FirebaseDatabaseProvider)
    {
        firebaseDatabaseProvider.fetchCurrenciesData { dataSnapshot ->
            Log.d("Synchronization", "Started Syncing Currencies")
            val currencies = mutableMapOf<String, Currency>()
            for (snapshotItem in dataSnapshot.children) {
                val currency = snapshotItem.getValue(Currency::class.java)
                if (currency!=null) {
                    currencies[currency.code] = currency
                }
            }
            AustromApplication.activeCurrencies = Currency.switchRatesToNewBaseCurrency(Currency.localizeCurrencyNames(currencies, context), appUser?.baseCurrencyCode)
            localDBProvider.deleteAllCurrencies()
            currencies.forEach { currency ->
                localDBProvider.writeCurrency(currency.value)
            }
            Log.d("Synchronization", "Finished Syncing Currencies")
        }
    }

    private suspend fun setReceivedInvitationListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        firebaseDatabaseProvider.fetchReceivedInvitationsData(appUser!!) { dataSnapshot ->
            Log.d("Synchronization", "Started Syncing Received Invitations")
            localDBProvider.recallInvitationsToUser(appUser!!.userId)
            dataSnapshot.children.forEach { snapshotItem ->
                val invitingBudget = firebaseDatabaseProvider.getBudgetById(snapshotItem.key.toString())
                if (invitingBudget!=null) {
                    val invitation = Invitation(
                        userId = appUser!!.userId,
                        budgetId = invitingBudget.budgetId,
                        token = snapshotItem.getValue(String::class.java).toString(),
                        providedEmail = appUser!!.email,
                        invitationCode = ""
                    )
                    if (localDBProvider.getInvitationsByUserIdAndBudgetId(appUser!!.userId, invitingBudget.budgetId)==null) {
                        localDBProvider.insertInvitation(invitation)
                    }
                }
            }
            Log.d("Synchronization", "Finished Syncing Received Invitations")
        }
    }

    private fun setSentInvitationsListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        if (budget==null) return
        val localSentInvitations = localDBProvider.getInvitationsOfBudget(budget.budgetId)
        localSentInvitations.forEach { invitation ->
            if (!firebaseDatabaseProvider.isUserInvitedToBudget(invitation.userId, invitation.budgetId)) {
                localDBProvider.recallInvitationToBudgetToUser(budget.budgetId, invitation.userId)
            }
        }

        firebaseDatabaseProvider.fetchSentInvitationsData(budget) { dataSnapshot, action ->
            Log.d("Synchronization", "Started Syncing Invitations")
            when (action) {
                "Added" -> {
                    if (localDBProvider.getInvitationsByUserIdAndBudgetId(dataSnapshot.key.toString(), budget.budgetId)==null) {
                        val invitedUser =  firebaseDatabaseProvider.getUserByUserId(dataSnapshot.key.toString())
                        if (invitedUser!=null) {
                            localDBProvider.insertInvitation(Invitation(
                                userId = dataSnapshot.key.toString(),
                                budgetId = budget.budgetId,
                                token = "",
                                providedEmail =invitedUser.email,
                                invitationCode = "********"
                            ))
                        } else {
                            val dummyUser = User(
                                username = "",
                                email = "",
                                password = ""
                            )
                            dummyUser.userId=dataSnapshot.key.toString()
                            firebaseDatabaseProvider.deleteInvitationToUser(dummyUser, budget)
                        }
                    }
                }
                "Removed" -> {
                    localDBProvider.recallInvitationToBudgetToUser(budget.budgetId, dataSnapshot.key.toString())
                }
                else  -> {}
            }
            Log.d("Synchronization", "Finished Syncing Invitations")
        }
    }

    private suspend fun setUsersListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        if (budget==null) {
            localDBProvider.getAllUsers().forEach{(id, user) -> if (appUser!!.userId!=id) localDBProvider.deleteUser(user)}
            return
        }
        firebaseDatabaseProvider.fetchUserData(budget) { dataSnapshot ->
            Log.d("Synchronization", "Started Syncing Users")
            val budgetUserKeys = mutableListOf<String>()
            for (snapshotItem in dataSnapshot.children) {
                val userID = snapshotItem.getValue(String::class.java)
                if (userID!=null) {
                    budgetUserKeys.add(userID)
                    val user = firebaseDatabaseProvider.getUserByUserId(userID)
                    if (user!= null) {
                        val localUser = localDBProvider.getUserByUserId(userID)
                        if (userID == appUser?.userId) {
                            user.password = appUser!!.password
                            user.tokenId = appUser!!.tokenId
                        }
                        if (localUser != null) {
                            localDBProvider.updateUser(user)
                        } else {
                            localDBProvider.writeNewUser(user)
                        }
                        knownUsers[user.userId] = user
                    }
                }
            }
            localDBProvider.getAllUsers().forEach { (id, user) -> if (!budgetUserKeys.contains(id) && appUser!!.userId!=id) {
                localDBProvider.deleteUser(user)
                knownUsers.remove(id)
            }}
            Log.d("Synchronization", "Finished Syncing Users")
        }
    }

    private suspend fun setAssetListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        if (budget==null) return
        firebaseDatabaseProvider.fetchAssetData(budget) {dataSnapshot ->
            Log.d("Synchronization", "Started Syncing Assets")
            val encryptionManager = EncryptionManager()
            for (snapshotItem in dataSnapshot.children) {
                val localAsset = localDBProvider.getAssetById(snapshotItem.key.toString())
                if (localAsset!=null && snapshotItem.value=="-") {
                    localAsset.delete(localDBProvider)
                    if (AustromApplication.activeAssets.containsKey(localAsset.assetId)) {
                        AustromApplication.activeAssets.remove(localAsset.assetId)
                    }
                } else {
                    val asset = encryptionManager.decryptAsset(snapshotItem.getValue(String::class.java).toString(),
                        encryptionManager.convertStringToSecretKey(appUser!!.tokenId))
                    if (localAsset!=null) {
                        localDBProvider.updateAsset(asset)
                    } else {
                        localDBProvider.createNewAsset(asset)
                    }
                    AustromApplication.activeAssets[asset.assetId] = asset
                }
            }
            Log.d("Synchronization", "Finished Syncing Assets")
        }
    }

    private suspend fun setCategoriesListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        if (budget==null) return
        firebaseDatabaseProvider.fetchCategoriesData(budget) {dataSnapshot ->
            Log.d("Synchronization", "Started Syncing Categories")
            val encryptionManager = EncryptionManager()
            val remoteCategoriesId = mutableListOf<String>()
            dataSnapshot.children.forEach {snapshotItem ->
                val localCategory = localDBProvider.getCategoryById(snapshotItem.key.toString())
                remoteCategoriesId.add(snapshotItem.key.toString())
                if (localCategory!=null) {
                    if (snapshotItem.value=="-") {
                        localDBProvider.deleteCategory(localCategory)
                        if (activeCategories.containsKey(localCategory.categoryId)) {
                            activeCategories.remove(localCategory.categoryId)
                        }
                    } else {
                        val category = encryptionManager.decryptCategory(snapshotItem.getValue(String::class.java).toString(),
                            encryptionManager.convertStringToSecretKey(appUser!!.tokenId))
                        localDBProvider.updateCategory(category)
                    }
                } else {
                    if (snapshotItem.value!="-") {
                        val category = encryptionManager.decryptCategory(snapshotItem.getValue(String::class.java).toString(),
                            encryptionManager.convertStringToSecretKey(appUser!!.tokenId))
                        localDBProvider.writeCategory(category)
                        activeCategories[category.categoryId] = category
                    }
                }
            }
            localDBProvider.getCategories().filter { l -> l.transactionType!=TransactionType.TRANSFER }.forEach { category ->
                if (!remoteCategoriesId.contains(category.categoryId)) {
                    localDBProvider.deleteCategory(category)
                    if (activeCategories.containsKey(category.categoryId)) activeCategories.remove(category.categoryId)
                }
            }
            Log.d("Synchronization", "Finished Syncing Categories")
        }
    }

    private suspend fun setTransactionListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        if (budget==null) return
        firebaseDatabaseProvider.fetchTransactionData(budget) {dataSnapshot ->
            Log.d("Synchronization", "Started Syncing Transactions")
            val encryptionManager = EncryptionManager()
            for (snapshotItem in dataSnapshot.children) {
                val localTransaction = localDBProvider.getTransactionByID(snapshotItem.key.toString())
                if (localTransaction==null) {
                    if (snapshotItem.value!="-") {
                        val remoteVersion = snapshotItem.getValue(String::class.java).intAfterLastPipe()
                        val transaction = encryptionManager.decryptTransaction(snapshotItem.getValue(String::class.java).substringBeforeLastPipe().toString(),
                            encryptionManager.convertStringToSecretKey(appUser!!.tokenId))
                        if (remoteVersion!=null) transaction.version = remoteVersion
                        val asset = AustromApplication.activeAssets[transaction.assetId]
                        if (asset!=null) {
                            localDBProvider.insertNewTransaction(transaction)
                        }
                        //TODO("What if asset of this transaction doesn't exist?")
                    }
                } else {
                    if (snapshotItem.value =="-") {
                        localDBProvider.deleteTransaction(localTransaction)
                    } else {
                        val remoteVersion = snapshotItem.getValue(String::class.java).intAfterLastPipe()
                        if (remoteVersion!=null && remoteVersion>localTransaction.version) {
                            val transaction = encryptionManager.decryptTransaction(snapshotItem.getValue(String::class.java).substringBeforeLastPipe().toString(),
                                encryptionManager.convertStringToSecretKey(appUser!!.tokenId))
                            transaction.version = remoteVersion
                            localDBProvider.updateTransaction(transaction)
                        }
                    }
                }
            }
            Log.d("Synchronization", "Finished Syncing Transactions")
        }
    }

    private suspend fun setTransactionDetailListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        if (budget==null) return
        firebaseDatabaseProvider.fetchTransactionDetailsData(budget) {dataSnapshot ->
            Log.d("Synchronization", "Started Syncing Transaction Details")
            val encryptionManager = EncryptionManager()
            for (snapshotItem in dataSnapshot.children) {
                val localTransactionDetail = localDBProvider.getTransactionDetailById(snapshotItem.key.toString())
                if (localTransactionDetail==null) {
                    if (snapshotItem.value!="-") {
                        val transactionDetail = encryptionManager.decryptTransactionDetail(snapshotItem.getValue(String::class.java).toString(), encryptionManager.convertStringToSecretKey(
                            appUser!!.tokenId))
                        localDBProvider.writeNewTransactionDetail(transactionDetail)
                    }
                } else {
                    if (snapshotItem.value =="-") {
                        localDBProvider.removeTransactionDetail(localTransactionDetail)
                    }
                }
            }
            Log.d("Synchronization", "Finished Syncing Transaction Details")
        }
    }
}

@Entity(tableName = "change_log")
data class AssetChangeLog(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val entityId: Long,
    val action: String,
    val currentVersion: Int
)

