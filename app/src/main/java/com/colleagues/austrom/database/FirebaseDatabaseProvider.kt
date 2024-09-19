package com.colleagues.austrom.database

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FirebaseDatabaseProvider(private val activity: FragmentActivity) : IDatabaseProvider{
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun createNewUser(user: User): String? {
        val reference = database.getReference("users")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the user")
            return null
        }
        user.username = user.username?.lowercase()
        user.email = user.email?.lowercase()
        reference.child(key).setValue(user)
        Log.w("Debug", "New user added to DB with key: $key")
        return  key
    }

    override fun updateUser(user: User) {
        val userKey = user.userId
        if (!userKey.isNullOrEmpty()) {
            user.userId=null
            user.username = user.username?.lowercase()
            user.email = user.email?.lowercase()
            database.getReference("users").child(userKey).setValue(user)
            user.userId=userKey
            Log.w("Debug", "User entry with key ${user.userId} updated")
        } else {
            Log.w("Debug", "Provided user without id. Update canceled")
        }
    }

    override fun deleteUser(user: User) {
        if (user.userId!=null) {
            database.getReference("users").child(user.userId!!).setValue(null)
            Log.w("Debug", "User entry with key ${user.userId} deleted")
        } else {
            Log.w("Debug", "Provided user without id. Delete canceled")
        }
    }

    override fun getUserByUserId(userId: String) : User? {
        var user : User? = null
        activity.lifecycleScope.launch {
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
                if (user !=null) {
                    user.userId = snapshot.key
                }
                user
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun getUserByUsername(username: String) : User? {
        var user : User? = null
        activity.lifecycleScope.launch {
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
                    if (user != null) {
                        user.userId = snapshot.children.elementAt(0).key
                    }
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
        activity.lifecycleScope.launch {
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
                    if (user != null) {
                        user.userId = snapshot.children.elementAt(0).key
                    }
                    user
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun createNewBudget(budget: Budget) : String? {
        val reference = database.getReference("budgets")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the budget")
            return null
        }
        reference.child(key).setValue(budget)
        Log.w("Debug", "New budget added to DB with key: $key")
        return  key
    }

    override fun updateBudget(budget: Budget) {
        val budgetKey = budget.budgetId
        if (!budgetKey.isNullOrEmpty()) {
            budget.budgetId = null
            database.getReference("budgets").child(budgetKey).setValue(budget)
            budget.budgetId = budgetKey
            Log.w("Debug", "Budget entry with key ${budget.budgetId} updated")
        } else {
            Log.w("Debug", "Provided budget without id. Update canceled")
        }
    }

    override fun deleteBudget(budget: Budget) {
        if (budget.budgetId!=null) {
            database.getReference("budgets").child(budget.budgetId!!).setValue(null)
            Log.w("Debug", "Budget entry with key ${budget.budgetId} deleted")
        } else {
            Log.w("Debug", "Provided budget without id. Delete canceled")
        }
    }


    override fun getBudgetById(budgetId: String) : Budget? {
        var budget : Budget? = null
        activity.lifecycleScope.launch {
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
                    if (budget!=null) {
                        budget.budgetId = snapshot.key
                    }
                    budget
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun createNewAsset(asset: Asset): String? {
        val reference = database.getReference("assets")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the asset")
            return null
        }
        reference.child(key).setValue(asset)
        Log.w("Debug", "New asset added to DB with key: $key")
        return key
    }

    override fun updateAsset(asset: Asset) {
        val assetKey = asset.assetId
        if (!assetKey.isNullOrEmpty()) {
            asset.assetId = null
            database.getReference("assets").child(assetKey).setValue(asset)
            asset.assetId = assetKey
            Log.w("Debug", "Asset entry with key ${asset.assetId} updated")
        } else {
            Log.w("Debug", "Provided asset without id. Update canceled")
        }
    }

    override fun deleteAsset(asset: Asset) {
        if (!asset.assetId.isNullOrEmpty()) {
            database.getReference("assets").child(asset.assetId!!).setValue(null)
            Log.w("Debug", "Asset entry with key ${asset.assetId} deleted")
        } else {
            Log.w("Debug", "Provided asset without id. Delete canceled")
        }
    }

    override fun getAssetsOfUser(user: User): MutableMap<String, Asset> {
        var assets : MutableMap<String, Asset> = mutableMapOf()
        activity.lifecycleScope.launch {
            assets = getAssetsOfUserAsync(user)
        }
        return assets
    }


    private fun getAssetsOfUserAsync(user: User) : MutableMap<String, Asset> {
        val reference = database.getReference("assets")
        val databaseQuery = reference.orderByChild("userId").equalTo(user.userId)
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                val assetsList = mutableMapOf<String, Asset>()
                for (child in snapshot.children) {
                    val asset = child.getValue(Asset::class.java)
                    if (asset!=null) {
                        asset.assetId = child.key
                        assetsList[child.key.toString()] = asset
                    }
                }
                assetsList
            }
            catch (e: Exception) {
                mutableMapOf()
            }
        }
    }

    override fun getAssetsOfBudget(budget: Budget): MutableMap<String, Asset> {
        var assets : MutableMap<String, Asset> = mutableMapOf()
        activity.lifecycleScope.launch {
            assets = getAssetsOfBudgetAsync(budget)
        }
        return assets
    }

    private fun getAssetsOfBudgetAsync(budget: Budget): MutableMap<String, Asset> {
        val assetsList = mutableMapOf<String, Asset>()
        val reference = database.getReference("assets")
        return  runBlocking {
            for (userId in budget.users!!) {
                val databaseQuery = reference.orderByChild("userId").equalTo(userId)
                try {
                    val snapshot = databaseQuery.get().await()
                    for (child in snapshot.children) {
                        val asset = child.getValue(Asset::class.java)
                        if (asset!=null) {
                            asset.assetId = child.key.toString()
                            assetsList[child.key.toString()] = asset
                        }
                    }
                }
                catch (e: Exception) {
                    continue
                }
            }
            assetsList
        }
    }

    override fun writeNewTransaction(transaction: Transaction): String? {
        val reference = database.getReference("transactions")
        val key = reference.push().key
        if (key == null) {
            Log.w("Debug", "Couldn't get push key for the transaction")
            return null
        }
        reference.child(key).setValue(transaction)
        Log.w("Debug", "New transaction added to DB with key: $key")
        return key
    }

    override fun getTransactionsOfUser(user: User): MutableList<Transaction> {
        var transactions : MutableList<Transaction> = mutableListOf()
        activity.lifecycleScope.launch {
            transactions = getTransactionsOfUserAsync(user)
        }
        return transactions
    }


    private fun getTransactionsOfUserAsync(user: User) : MutableList<Transaction> {
        val reference = database.getReference("transactions")
        val databaseQuery = reference.orderByChild("userId").equalTo(user.userId)
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
                transactionsList
            }
            catch (e: Exception) {
                mutableListOf()
            }
        }
    }

    override fun getTransactionsOfBudget(budget: Budget): MutableList<Transaction> {
        var transactions : MutableList<Transaction> = mutableListOf()
        activity.lifecycleScope.launch {
            transactions = getTransactionOfBudgetAsync(budget)
        }
        return transactions
    }

    private fun getTransactionOfBudgetAsync(budget: Budget): MutableList<Transaction> {
        val transactionsList = mutableListOf<Transaction>()
        val reference = database.getReference("transactions")
        return runBlocking {
            for (userId in budget.users!!) {
                val databaseQuery = reference.orderByChild("userId").equalTo(userId)
                try {
                    val snapshot = databaseQuery.get().await()
                    for (child in snapshot.children) {
                        val transaction = child.getValue(Transaction::class.java)
                        if (transaction!=null) {
                            transaction.transactionId = child.key
                            transaction.transactionDate = parseIntDateToDate(transaction.transactionDateInt)
                            transactionsList.add(transaction)
                        }
                    }
                }
                catch (e: Exception) {
                    continue
                }
            }
            transactionsList
        }
    }

    override fun getCurrencies(): MutableMap<String, Currency> {
        var currencies : MutableMap<String, Currency> = mutableMapOf()
        activity.lifecycleScope.launch {
            currencies = getCurrenciesAsync()
        }
        return currencies
    }

    private fun getCurrenciesAsync(): MutableMap<String, Currency> {
        val databaseQuery = database.getReference("currencies")
        val currenciesList = mutableMapOf<String, Currency>()
        return runBlocking {
            try {
                val snapshot = databaseQuery.get().await()
                for (child in snapshot.children) {
                    val currency = child.getValue(Currency::class.java)
                    if (currency!=null) {
                        currenciesList[child.key.toString()] = currency
                    }
                }
                currenciesList
            }
            catch (e: Exception) {
                mutableMapOf()
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