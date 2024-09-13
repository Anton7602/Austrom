package com.colleagues.austrom.database

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FirebaseDatabaseProvider : IDatabaseProvider{
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun writeNewUser(user: User) {
        val reference = database.getReference("users")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the user")
            return
        }
        reference.child(key).setValue(user)
        Log.w("Debug", "New user added to DB with key: $key")
    }

    override fun getUserByUsername(username: String, activity: FragmentActivity?) : User? {
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
                    snapshot.children.elementAt(0).getValue(User::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun writeNewBudget(budget: Budget) {
        val reference = database.getReference("budgets")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the budget")
            return
        }
        reference.child(key).setValue(budget)
        Log.w("Debug", "New budget added to DB with key: $key")
    }

    override fun getBudgetById(budgetId: String, activity: FragmentActivity?) : Budget? {
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
                    snapshot.children.elementAt(0).getValue(Budget::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun writeNewAsset(asset: Asset) {
        val reference = database.getReference("assets")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the asset")
            return
        }
        reference.child(key).setValue(asset)
        Log.w("Debug", "New asset added to DB with key: $key")
    }

    override fun getAssetsOfUser(user: User, activity: FragmentActivity?): MutableList<Asset>? {
        var assets : MutableList<Asset>? = mutableListOf()
        activity?.lifecycleScope?.launch {
            assets = getAssetsOfUserAsync(user)
        }
        return assets
    }


    private fun getAssetsOfUserAsync(user: User) : MutableList<Asset>? {
        val reference = database.getReference("assets")
        val databaseQuery = reference.orderByChild("user_id").equalTo(user.userId)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                val assetsList = mutableListOf<Asset>()
                for (child in snapshot.children) {
                    val asset = child.getValue(Asset::class.java)
                    asset?.assetId = child.key
                    asset?.let { assetsList.add(it) }
                }
                assetsList.ifEmpty {
                    null
                }
            }
            catch (e: Exception) {
                null
            }
        }
    }

    override fun writeNewTransaction(transaction: Transaction) {
        val reference = database.getReference("transactions")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the transaction")
            return
        }
        reference.child(key).setValue(transaction)
        Log.w("Debug", "New transaction added to DB with key: $key")
    }

    override fun getTransactionsOfUser(user: User, activity: FragmentActivity?): MutableList<Transaction>? {
        var transactions : MutableList<Transaction>? = mutableListOf()
        activity?.lifecycleScope?.launch {
            transactions = getTransactionsOfUserAsync(user)
        }
        return transactions
    }


    private fun getTransactionsOfUserAsync(user: User) : MutableList<Transaction>? {
        val reference = database.getReference("transactions")
        val databaseQuery = reference.orderByChild("userID").equalTo(user.userId)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                val transactionsList = mutableListOf<Transaction>()
                for (child in snapshot.children) {
                    val transaction = child.getValue(Transaction::class.java)
                    if (transaction!=null) {
                        transaction.transactionId = child.key
                        transaction.transactionDate = parseIntDateToDate(transaction.transactionDateInt)
                        transactionsList.add(transaction)
                    }
                }
                transactionsList.ifEmpty {
                    null
                }
            }
            catch (e: Exception) {
                null
            }
        }
    }

    fun parseDateToIntDate(date: LocalDate) : Int {
        return (date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))).toInt()
    }

    fun parseIntDateToDate(intDate: Int?) : LocalDate {
        if (intDate==null || intDate.toString().length!=8) return LocalDate.now()
        val year = intDate.toString().substring(0,4).toInt()
        val month = intDate.toString().substring(4,6).toInt()
        val day = intDate.toString().substring(6).toInt()
        return LocalDate.of(year, month, day)
    }
}