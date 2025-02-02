package com.colleagues.austrom.database

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.managers.EncryptionManager
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Invitation
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseDatabaseProvider(private val activity: FragmentActivity?) : IRemoteDatabaseProvider{
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()


    //region User
    override fun createNewUser(user: User) {
        val reference = database.getReference("users")
        val encryptionManager = EncryptionManager()
        val password = user.password
        user.password = encryptionManager.hashPassword(user.password)
        reference.child(user.userId).setValue(user)
        user.password = password
    }

    override fun updateUser(user: User) {
        val password = user.password
        val token = user.tokenId
        val encryptionManager = EncryptionManager()
        if (token!=null) {
            user.tokenId = encryptionManager.encrypt(token, encryptionManager.generateEncryptionKey(user.password, user.userId.toByteArray()))
        }
        user.password = encryptionManager.hashPassword(user.password)
        database.getReference("users").child(user.userId).setValue(user)
        user.password = password
        user.tokenId = token
    }

    override fun deleteUser(user: User) { database.getReference("users").child(user.userId).setValue(null) }

    override fun getUserByUserId(userId: String) : User? {
        var user : User? = null
        activity?.lifecycleScope?.launch {
            user = getUserByUserIdAsync(userId)
        }
        return user
    }

    private suspend fun getUserByUserIdAsync(userId: String) : User? {
        val reference = database.getReference("users")
        val databaseQuery = reference.child(userId)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                val user = snapshot.getValue(User::class.java)
                user
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun getUserByUsername(username: String) : User? {
        var user : User? = null
        activity?.lifecycleScope?.launch {
            user = getUserByUsernameAsync(username)
        }
        return user
    }

    private suspend fun getUserByUsernameAsync(username: String) : User? {
        val reference = database.getReference("users")
        val databaseQuery = reference.orderByChild("username").equalTo(username).limitToFirst(1)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                if (snapshot.childrenCount>0) {
                    val user = snapshot.children.elementAt(0).getValue(User::class.java)
                    user
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun getUserByEmail(email: String) : User? {
        var user : User? = null
        activity?.lifecycleScope?.launch {
            user = getUserByEmailAsync(email)
        }
        return user
    }

    private suspend fun getUserByEmailAsync(email: String) : User? {
        val reference = database.getReference("users")
        val databaseQuery = reference.orderByChild("email").equalTo(email.lowercase()).limitToFirst(1)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                if (snapshot.childrenCount>0) {
                    val user = snapshot.children.elementAt(0).getValue(User::class.java)
                    user
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun getUsersByBudget(budgetId: String) : MutableMap<String, User> {
        var users : MutableMap<String, User> = mutableMapOf()
        activity?.lifecycleScope?.launch {
            users = getUsersByBudgetAsync(budgetId)
        }
        return users
    }

    private suspend fun getUsersByBudgetAsync(budgetId: String) : MutableMap<String, User> {
        val reference = database.getReference("users")
        val databaseQuery = reference.orderByChild("activeBudgetId").equalTo(budgetId)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                val usersList = mutableMapOf<String, User>()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user!=null) {
                        usersList[child.key.toString()] = user
                    }
                }
                usersList
            }
            catch (e: Exception) {
                mutableMapOf()
            }
        }
    }
    //endregion

    //region Budget
    override fun createNewBudget(budget: Budget) { database.getReference("budgets").child(budget.budgetId).setValue(budget) }
    override fun updateBudget(budget: Budget) { database.getReference("budgets").child(budget.budgetId).setValue(budget) }
    override fun deleteBudget(budget: Budget) { database.getReference("budgets").child(budget.budgetId).setValue(null)  }


    override fun getBudgetById(budgetId: String) : Budget? {
        var budget : Budget? = null
        activity?.lifecycleScope?.launch {
            budget = getBudgetByIdAsync(budgetId)
        }
        return budget
    }

    private fun getBudgetByIdAsync(budgetId: String) : Budget? {
        val reference = database.getReference("budgets")
        val databaseQuery = reference.child(budgetId)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                if (snapshot.childrenCount>0) {
                    val budget = snapshot.getValue(Budget::class.java)
                    budget
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    //endregion

    //region Asset
    override fun createNewAsset(asset: Asset, budget: Budget) {
        if (AustromApplication.appUser?.tokenId!=null) {
            val encryptionManager = EncryptionManager()
            database.getReference("assets").child(budget.budgetId).child(asset.assetId)
                .setValue(encryptionManager.encrypt(asset, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }

    override fun updateAsset(asset: Asset, budget: Budget) {
        if (AustromApplication.appUser?.tokenId!=null) {
            val encryptionManager = EncryptionManager()
            database.getReference("assets").child(budget.budgetId).child(asset.assetId)
                .setValue(encryptionManager.encrypt(asset, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }

    override fun deleteAsset(asset: Asset, budget: Budget) {
        database.getReference("assets").child(budget.budgetId).child(asset.assetId).setValue("-")
    }

    override fun deleteAssetsOfBudget(budget: Budget) {
        database.getReference("assets").child(budget.budgetId).setValue(null)
    }
    //endregion

    //region Transaction
    override fun insertTransaction(transaction: Transaction, budget: Budget) {
        if (AustromApplication.appUser?.tokenId!=null) {
            val encryptionManager = EncryptionManager()
            database.getReference("transactions").child(budget.budgetId).child(transaction.transactionId)
                .setValue(encryptionManager.encrypt(transaction, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }

    override fun updateTransaction(transaction: Transaction, budget: Budget) {
        if (AustromApplication.appUser?.tokenId!=null) {
            val encryptionManager = EncryptionManager()
            database.getReference("transactions").child(budget.budgetId).child(transaction.transactionId)
                .setValue(encryptionManager.encrypt(transaction, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }

    override fun deleteTransaction(transaction: Transaction, budget: Budget) {
        database.getReference("transactions").child(budget.budgetId).child(transaction.transactionId).setValue("-")
    }

    fun conductTransaction(transaction: Transaction, budget: Budget) {
        if (AustromApplication.appUser?.tokenId!=null && AustromApplication.activeAssets[transaction.assetId]!=null) {
            val encryptionManager = EncryptionManager()
            database.getReference("transactions").child(budget.budgetId).child(transaction.transactionId)
                .setValue(encryptionManager.encrypt(transaction, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId))+"|${transaction.version}")
            database.getReference("assets").child(budget.budgetId).child(transaction.assetId)
                .setValue(encryptionManager.encrypt(AustromApplication.activeAssets[transaction.assetId]!!, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }

    fun cancelTransaction(transaction: Transaction, budget: Budget, linkedTransaction: Transaction? = null) {
        val encryptionManager = EncryptionManager()
        database.getReference("transactions").child(budget.budgetId).child(transaction.transactionId).setValue("-")
        if (linkedTransaction!=null) {
            database.getReference("transactions").child(budget.budgetId).child(linkedTransaction.transactionId).setValue("-")
        }
        database.getReference("assets").child(budget.budgetId).child(transaction.assetId)
            .setValue(encryptionManager.encrypt(AustromApplication.activeAssets[transaction.assetId]!!, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        if (linkedTransaction!=null) {
            database.getReference("assets").child(budget.budgetId).child(linkedTransaction.assetId)
                .setValue(encryptionManager.encrypt(AustromApplication.activeAssets[linkedTransaction.assetId]!!, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }

    override fun deleteTransactionsOfBudget(budget: Budget) {
        database.getReference("transactions").child(budget.budgetId).setValue(null)
    }
    //endregion

    override fun insertCategory(category: Category, budget: Budget) {
        if (AustromApplication.appUser?.tokenId!=null) {
            val encryptionManager = EncryptionManager()
            database.getReference("categories").child(budget.budgetId).child(category.categoryId)
                .setValue(encryptionManager.encrypt(category, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }

    override fun updateCategory(category: Category, budget: Budget) {
        if (AustromApplication.appUser!!.tokenId!=null) {
            val encryptionManager = EncryptionManager()
            database.getReference("categories").child(budget.budgetId).child(category.categoryId)
                .setValue(encryptionManager.encrypt(category, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }

    override fun deleteCategoriesOfBudget(budget: Budget) {
        database.getReference("categories").child(budget.budgetId).setValue(null)
    }

    override fun deleteCategory(category: Category, budget: Budget) {
        database.getReference("categories").child(budget.budgetId).child(category.categoryId).setValue("-")
    }

    //region TransactionDetail
    override fun insertTransactionDetail(transactionDetail: TransactionDetail, budget: Budget) {
        if (AustromApplication.appUser?.tokenId!=null) {
            val encryptionManager = EncryptionManager()
            database.getReference("transactionDetails").child(budget.budgetId).child(transactionDetail.transactionDetailId)
                .setValue(encryptionManager.encrypt(transactionDetail, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }

    fun deleteTransactionDetail(transactionDetail: TransactionDetail) {
        database.getReference("transactionDetails").child(transactionDetail.transactionDetailId).setValue(null)
    }

    override fun deleteTransactionDetailsOfBudget(budget: Budget) {
        database.getReference("transactionDetails").child(budget.budgetId).setValue(null)
    }
    //endregion

    override fun sentBudgetInvite(invitation: Invitation) {
        val encryptionManager = EncryptionManager()
        database.getReference("invitations").child(invitation.userId)
            .setValue(encryptionManager.encrypt(invitation, encryptionManager.generateEncryptionKey(invitation.invitationCode, invitation.invitationCode.toByteArray())))
    }



//    override fun getCurrencies(): MutableMap<String, Currency> {
//        var currencies : MutableMap<String, Currency> = mutableMapOf()
//        activity?.lifecycleScope?.launch {
//            currencies = getCurrenciesAsyncOld()
//        }
//        return currencies
//    }
//
//    fun getCurrenciesAsyncOld(): MutableMap<String, Currency> {
//        val databaseQuery = database.getReference("currencies")
//        val currenciesList = mutableMapOf<String, Currency>()
//        return runBlocking {
//            try {
//                val snapshot = databaseQuery.get().await()
//                for (child in snapshot.children) {
//                    val currency = child.getValue(Currency::class.java)
//                    if (currency!=null) {
//                        currenciesList[child.key.toString()] = currency
//                    }
//                }
//                currenciesList
//            }
//            catch (e: Exception) {
//                mutableMapOf()
//            }
//        }
//    }

    //Sync
//    fun setCurrenciesListener(listener: ValueEventListener) { database.getReference("currencies").addValueEventListener(listener) }
//    fun setUserListener(budget: Budget, listener: ValueEventListener) { database.getReference("budgets").child(budget.budgetId).child("users").addValueEventListener(listener) }
//    fun setAssetListener(budget: Budget, listener: ValueEventListener) { database.getReference("assets").child(budget.budgetId).addValueEventListener(listener) }
//    fun setTransactionListener(budget: Budget, listener: ValueEventListener) { database.getReference("transactions").child(budget.budgetId).addValueEventListener(listener) }
//    fun setTransactionDetailListener(budget: Budget, listener: ValueEventListener) { database.getReference("transactionDetails").child(budget.budgetId).addValueEventListener(listener) }

    suspend fun fetchCurrenciesData(processSnapshot: (DataSnapshot) -> Unit) { fetchDataSnapshot(database.getReference("currencies")) {dataSnapshot -> processSnapshot(dataSnapshot) } }
    suspend fun fetchUserData(budget: Budget, processSnapshot: (DataSnapshot) -> Unit) {
        fetchDataSnapshot(database.getReference("budgets").child(budget.budgetId).child("users"))  {dataSnapshot -> processSnapshot(dataSnapshot) }
    }
    suspend fun fetchCategoriesData(budget: Budget,processSnapshot: (DataSnapshot) -> Unit) {
        fetchDataSnapshot(database.getReference("categories").child(budget.budgetId)) {dataSnapshot -> processSnapshot(dataSnapshot) }
    }
    suspend fun fetchAssetData(budget: Budget,processSnapshot: (DataSnapshot) -> Unit) {
        fetchDataSnapshot(database.getReference("assets").child(budget.budgetId)) {dataSnapshot -> processSnapshot(dataSnapshot) }
    }
    suspend fun fetchTransactionData(budget: Budget,processSnapshot: (DataSnapshot) -> Unit) {
        fetchDataSnapshot(database.getReference("transactions").child(budget.budgetId)) {dataSnapshot -> processSnapshot(dataSnapshot) }
    }
    suspend fun fetchTransactionDetailsData(budget: Budget,processSnapshot: (DataSnapshot) -> Unit) {
        fetchDataSnapshot(database.getReference("transactionDetails").child(budget.budgetId)) {dataSnapshot -> processSnapshot(dataSnapshot) }
    }


    private suspend fun fetchDataSnapshot(databaseReference: DatabaseReference, processSnapshot: (DataSnapshot) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            var isResumed = false

            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!isResumed) {
                        isResumed = true
                        try {
                            processSnapshot(dataSnapshot)
                            continuation.resume(Unit)
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    if (!isResumed) { // Check if the continuation has already been resumed
                        isResumed = true
                        continuation.resumeWithException(databaseError.toException())
                    }
                }
            }

            databaseReference.addValueEventListener(listener)

            continuation.invokeOnCancellation {
                databaseReference.removeEventListener(listener)
            }
        }
    }
//endregion
}