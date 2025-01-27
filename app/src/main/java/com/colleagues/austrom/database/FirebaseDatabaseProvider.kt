package com.colleagues.austrom.database

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.managers.EncryptionManager
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

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
        val databaseQuery = reference.orderByChild("email").equalTo(email).limitToFirst(1)
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
    //endregion

    //region Transaction
    override fun createNewTransaction(transaction: Transaction, budget: Budget) {
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
    //endregion



    //region TransactionDetail
    fun createNewTransactionDetail(transactionDetail: TransactionDetail, budget: Budget) {
        if (AustromApplication.appUser?.tokenId!=null) {
            val encryptionManager = EncryptionManager()
            database.getReference("transactionDetails").child(budget.budgetId).child(transactionDetail.transactionDetailId)
                .setValue(encryptionManager.encrypt(transactionDetail, encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId)))
        }
    }
    //endregion

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
    fun setCurrenciesListener(listener: ValueEventListener) { database.getReference("currencies").addValueEventListener(listener) }
    fun setAssetListener(listener: ValueEventListener, budget: Budget) { database.getReference("assets").child(budget.budgetId).addValueEventListener(listener) }
    fun setTransactionListener(listener: ValueEventListener, budget: Budget) { database.getReference("transactions").child(budget.budgetId).addValueEventListener(listener) }
    fun setTransactionDetailListener(listener: ValueEventListener, budget: Budget) { database.getReference("transactionDetails").child(budget.budgetId).addValueEventListener(listener) }
    //endregion
}