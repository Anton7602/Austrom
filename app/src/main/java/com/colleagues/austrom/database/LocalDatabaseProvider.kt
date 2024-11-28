package com.colleagues.austrom.database

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.models.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LocalDatabaseProvider(private val context: Context) {
    private val localDatabase = LocalDatabase.getDatabase(context)

    fun writeNewUser(user: User): String {
        val dao = localDatabase.userDao()
        if (user.userId.isEmpty()) {
            user.userId = User.generateUniqueAssetKey()
        }
        runBlocking {
            dao.insertUser(user)
        }
        return user.userId
    }

    fun updateUser(user: User) {
        val dao = localDatabase.userDao()
        runBlocking {
            dao.updateUser(user)
        }
    }

    fun deleteUser(user: User) {
        val dao = localDatabase.userDao()
        runBlocking {
            dao.deleteUser(user)
        }
    }

    fun getAllUsers(): MutableMap<String, User> {
        val dao = localDatabase.userDao()
        val users = mutableMapOf<String, User>()
        runBlocking {
            val userList = dao.getAllUsers()
            userList.forEach{user ->
                users[user.userId] = user
            }
        }
        return users
    }

    fun getUserByUserId(userId: String): User? {
        val dao = localDatabase.userDao()
        var user: User? = null;
        var users: List<User>
        runBlocking {
            users = dao.getUserByUserId(userId)
        }
        if (users.isNotEmpty()) {
            user = users[0]
        }
        return user
    }

    fun getUserByUsername(username: String): User? {
        val dao = localDatabase.userDao()
        var user: User? = null;
        var users: List<User>
        runBlocking {
            users = dao.getUserByUsername(username)
        }
        if (users.isNotEmpty()) {
            user = users[0]
        }
        return user
    }

//    fun getUserByEmail(email: String): User? {
//        TODO("Not yet implemented")
//    }

    fun createNewAsset(asset: Asset): String {
        val dao = localDatabase.assetDao()
        asset.assetId = Asset.generateUniqueAssetKey();
        runBlocking {
            dao.insertAsset(asset)
        }
        return asset.assetId
    }

    fun сreateNewAssetAsync(asset: Asset): String {
        val dao = localDatabase.assetDao()
        asset.assetId = Asset.generateUniqueAssetKey();
        runBlocking {
            dao.insertAsset(asset)
        }
        return asset.assetId
    }

    fun updateAsset(asset: Asset) {
        val dao = localDatabase.assetDao()
        runBlocking {
            dao.updateAsset(asset)
        }
    }

    fun deleteAsset(asset: Asset) {
        val dao = localDatabase.assetDao()
        runBlocking {
            dao.deleteAsset(asset)
        }
    }

    fun getAssetsOfUser(user: User): MutableMap<String, Asset> {
        val assetMap = mutableMapOf<String, Asset>()
        val dao = localDatabase.assetDao()
        runBlocking {
            val assets = dao.getAssetsOfUser(user.userId)
            for (asset in assets) {
                assetMap[asset.assetId] = asset
            }
        }
        return assetMap
    }

    fun getAssetsOfBudget(budget: Budget): MutableMap<String, Asset> {
        val assetMap = mutableMapOf<String, Asset>()
        val dao = localDatabase.assetDao()
        runBlocking {
            val assets = dao.getAssetsOfBudget()
            for (asset in assets) {
                assetMap[asset.assetId] = asset
            }
        }
        return assetMap
    }

//    override fun createNewBudget(budget: Budget): String? {
//        TODO("Not yet implemented")
//    }
//
//    override fun updateBudget(budget: Budget) {
//        TODO("Not yet implemented")
//    }
//
//    override fun deleteBudget(budget: Budget) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getBudgetById(budgetId: String): Budget? {
//        TODO("Not yet implemented")
//    }

    fun writeNewTransaction(transaction: Transaction): String {
        val dao = localDatabase.transactionDao()
        transaction.transactionId = Transaction.generateUniqueTransactionKey();
        runBlocking {
            dao.insertTransaction(transaction)
        }
        return transaction.transactionId
    }

    fun updateTransaction(transaction: Transaction) {
        val dao = localDatabase.transactionDao()
        runBlocking {
            dao.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        val dao = localDatabase.transactionDao()
        runBlocking {
            dao.deleteTransaction(transaction)
        }
    }

    fun getTransactionsOfUser(user: User): MutableList<Transaction> {
        val dao =  localDatabase.transactionDao()
        var transactions: MutableList<Transaction>
        runBlocking {
            transactions = dao.getTransactionsOfUser(user.userId.toString()).toMutableList()
        }
        return transactions
    }

    fun getTransactionsOfBudget(budget: Budget): MutableList<Transaction> {
        val dao =  localDatabase.transactionDao()
        var transactions: MutableList<Transaction>
        runBlocking {
            transactions = dao.getTransactionsOfBudget().toMutableList()
        }
        return transactions
    }

    fun getTransactionsOfAsset(asset: Asset): MutableList<Transaction> {
        val dao =  localDatabase.transactionDao()
        var transactions: MutableList<Transaction>
        runBlocking {
            transactions = dao.getTransactionsOfAsset(asset.assetId).toMutableList()
        }
        return transactions
    }

    fun getTransactionOfCategory(category: Category): MutableList<Transaction> {
        val dao = localDatabase.transactionDao()
        var transaction: MutableList<Transaction>
        runBlocking {
            transaction = dao.getTransactionsByCategoryId(category.id).toMutableList()
        }
        return transaction
    }

    fun writeNewTransactionDetail(transactionDetail: TransactionDetail): String {
        val dao = localDatabase.transactionDetailDao()
        transactionDetail.transactionDetailId = TransactionDetail.generateUniqueTransactionKey();
        runBlocking {
            dao.insertTransactionDetail(transactionDetail)
        }
        return transactionDetail.transactionDetailId
    }

    fun getTransactionDetailsOfTransaction(transaction: Transaction) : List<TransactionDetail> {
        val dao = localDatabase.transactionDetailDao()
        var transactionDetails: MutableList<TransactionDetail>
        runBlocking {
            transactionDetails = dao.getTransactionDetailsOfTransaction(transaction.transactionId).toMutableList()
        }
        return transactionDetails
    }

    fun writeCurrency(currency: Currency) {
        val dao = localDatabase.currencyDao()
        runBlocking {
            dao.insertCurrency(currency)
        }
    }

    fun getCurrencies(): MutableMap<String, Currency> {
        val dao = localDatabase.currencyDao()
        val currenciesMap = mutableMapOf<String, Currency>()
        runBlocking {
            val currencies = dao.getAllCurrencies()
            for (currency in currencies) {
                currenciesMap[currency.code] = currency
            }
        }
        return currenciesMap
    }

    fun deleteAllCurrencies() {
        runBlocking {
            localDatabase.currencyDao().deleteAllCurrencies()
        }
    }

    fun writeCategory(category: Category) {
        val dao = localDatabase.categoryDao()
        runBlocking {
            dao.insertCategory(category)
        }
    }

    fun getCategories(transactionType: TransactionType? = null): List<Category> {
        val dao = localDatabase.categoryDao()
        var categories: List<Category>
        runBlocking {
            categories = when (transactionType) {
                TransactionType.INCOME -> dao.getCategories(transactionType)
                TransactionType.EXPENSE -> dao.getCategories(transactionType)
                TransactionType.TRANSFER -> listOf()
                null -> dao.getAllCategories()
            }
        }
        return categories
    }

    fun getCategoryById(categoryId: String) : Category? {
        val dao = localDatabase.categoryDao()
        var category: Category? = null
        runBlocking {
            category = dao.getCategoryById(categoryId).first()
        }
        return category
    }

    fun updateCategory(category: Category) {
        val dao = localDatabase.categoryDao()
        runBlocking {
            dao.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        runBlocking {
            localDatabase.categoryDao().deleteCategory(category)
        }
    }
}
