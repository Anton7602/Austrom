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

    override fun createNewUser(user: User): String? {
        val reference = database.getReference("users")
        val encryptionManager = EncryptionManager()
        user.password = encryptionManager.hashPassword(user.password.toString())
        val key = user.userId
        user.userId = ""
        reference.child(key).setValue(user)
        user.userId =key
        Log.w("Debug", "New user added to DB with key: $user.userId")
        return  user.userId
    }

    override fun updateUser(user: User) {
        val userKey = user.userId
        if (userKey.isNotEmpty()) {
            database.getReference("users").child(userKey).setValue(user)
            Log.w("Debug", "User entry with key ${user.userId} updated")
        } else {
            Log.w("Debug", "Provided user without id. Update canceled")
        }
    }

    override fun deleteUser(user: User) {
        if (user.userId!=null) {
            database.getReference("users").child(user.userId).setValue(null)
            Log.w("Debug", "User entry with key ${user.userId} deleted")
        } else {
            Log.w("Debug", "Provided user without id. Delete canceled")
        }
    }

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
                if (user !=null) {
                    user.userId = snapshot.key.toString()
                }
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
                    if (user != null) {
                        user.userId = snapshot.children.elementAt(0).key.toString()
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
                    if (user != null) {
                        user.userId = snapshot.children.elementAt(0).key.toString()
                    }
                    user
                } else {
                    null
                }
            } catch (e: Exception) {
                var test = e.message
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
                        user.userId = child.key.toString()
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

    override fun getCurrencies(): MutableMap<String, Currency> {
        var currencies : MutableMap<String, Currency> = mutableMapOf()
        activity?.lifecycleScope?.launch {
            currencies = getCurrenciesAsyncOld()
        }
        return currencies
    }

    fun getCurrenciesAsyncOld(): MutableMap<String, Currency> {
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

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Debug", "loadPost:onCancelled", databaseError.toException())
            }
        }
        databaseReference.addValueEventListener(currencyListener)
    }

    private fun parseDateToIntDate(date: LocalDate) : Int {
        return (date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))).toInt()
    }

    private fun parseIntDateToDate(intDate: Int?) : LocalDate {
        if (intDate==null || intDate.toString().length!=8) return LocalDate.now()
        val year = intDate.toString().substring(0,4).toInt()
        val month = intDate.toString().substring(4,6).toInt()
        val day = intDate.toString().substring(6).toInt()
        return LocalDate.of(year, month, day)
    }
}