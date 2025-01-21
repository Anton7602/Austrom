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
import com.colleagues.austrom.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FirebaseDatabaseProvider(private val activity: FragmentActivity?) : IRemoteDatabaseProvider{
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

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

    fun setAssetListener(budget: Budget, localDBProvider: LocalDatabaseProvider) {
        val databaseReference = database.getReference("assets").child(budget.budgetId)
        val assetListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val encryptionManager = EncryptionManager()
                for (snapshotItem in dataSnapshot.children) {
                    val localAsset = localDBProvider.getAssetById(snapshotItem.key.toString())
                    if (localAsset==null) {
                        if (snapshotItem.value!="-") {
                            val asset = encryptionManager.decryptAsset(snapshotItem.getValue(String::class.java).toString(), encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId))
                            localDBProvider.createNewAsset(asset)
                        }
                    } else {
                        if (snapshotItem.value=="-") {
                            localAsset.delete(localDBProvider)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Debug", "loadPost:onCancelled", databaseError.toException())
            }
        }
        databaseReference.addValueEventListener(assetListener)
    }

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

    fun setTransactionListener(budget: Budget, localDBProvider: LocalDatabaseProvider) {
        val databaseReference = database.getReference("transactions").child(budget.budgetId)
        val transactionListener =  object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val encryptionManager = EncryptionManager()
                for (snapshotItem in dataSnapshot.children) {
                    val localTransaction = localDBProvider.getTransactionByID(snapshotItem.key.toString())
                    if (localTransaction==null) {
                        if (snapshotItem.value!="-") {
                            val transaction = encryptionManager.decryptTransaction(snapshotItem.getValue(String::class.java).toString(), encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId))
                            val asset = AustromApplication.activeAssets[transaction.assetId]
                            if (asset!=null) {
                                localDBProvider.submitNewTransaction(transaction, asset)
                            }
                            //TODO("What if asset of this transaction doesn't exist?")
                        }
                    } else {
                        if (snapshotItem.value =="-") {
                            localTransaction.cancel(localDBProvider)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Debug", "loadPost:onCancelled", databaseError.toException())
            }
        }
        databaseReference.addValueEventListener(transactionListener)
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

    fun setCurrenciesListener() {
        val databaseReference = database.getReference("currencies")
        val currencyListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currencies = mutableMapOf<String, Currency>()
                for (snapshotItem in dataSnapshot.children) {
                    val currency = snapshotItem.getValue(Currency::class.java)
                    if (currency!=null) {
                        currencies[currency.code] = currency
                    }
                }
                if (activity?.baseContext!=null) {
                    AustromApplication.activeCurrencies = Currency.switchRatesToNewBaseCurrency(
                        Currency.localizeCurrencyNames(currencies, activity.baseContext), AustromApplication.appUser?.baseCurrencyCode)
                    val dbProvider = LocalDatabaseProvider(activity.baseContext)
                    dbProvider.deleteAllCurrencies()
                    currencies.forEach { currency ->
                        dbProvider.writeCurrency(currency.value)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) { Log.w("Debug", "loadPost:onCancelled", databaseError.toException()) }
        }
        databaseReference.addValueEventListener(currencyListener)
    }
}