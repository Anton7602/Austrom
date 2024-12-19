package com.colleagues.austrom.database

import android.content.Context
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.InvalidTransactionException
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.models.TransactionValidationType
import com.colleagues.austrom.models.User
import kotlinx.coroutines.runBlocking

class LocalDatabaseProvider(private val context: Context) {
    private val localDatabase = LocalDatabase.getDatabase(context)

    fun writeNewUser(user: User): String {
        val dao = localDatabase.userDao()
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

    fun deleteAllUsers() {
        runBlocking {
            localDatabase.userDao().clearAllUsers()
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
        runBlocking {
            localDatabase.complexDao().deleteAssetWithTransactions(localDatabase.transactionDao(), localDatabase.assetDao(), asset)
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

    fun getAssetById(assetId: String): Asset? {
        var asset: Asset? = null
        val dao = localDatabase.assetDao()
        runBlocking {
            asset = dao.getAssetById(assetId).first()
        }
        return asset
    }


    fun submitNewTransaction(transaction: Transaction, asset: Asset) {
        runBlocking {
            localDatabase.complexDao().submitTransaction(localDatabase.transactionDao(), localDatabase.assetDao(), transaction, asset)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        val dao = localDatabase.transactionDao()
        runBlocking {
            dao.updateTransaction(transaction)
        }
    }

    fun cancelTransaction(transaction: Transaction) {
        val activeAsset = this.getAssetById(transaction.assetId) ?: throw InvalidTransactionException("Transaction cancellation failed. Asset is unknown!", TransactionValidationType.UNKNOWN_ASSET_INVALID)
        if (transaction.linkedTransactionId!=null) {
            val linkedTransaction = this.getTransactionByID(transaction.linkedTransactionId!!) ?: throw InvalidTransactionException("Transaction cancellation failed. Linked Transaction can't be found", TransactionValidationType.UNKNOWN_LINKED_TRANSACTION)
            val activeAssetOfLinkedTransaction = this.getAssetById(linkedTransaction.assetId) ?: throw InvalidTransactionException("Transaction cancellation failed. Asset of linked transaction is unknown!", TransactionValidationType.UNKNOWN_ASSET_INVALID)
            runBlocking {
                localDatabase.complexDao().cancelTransfer(localDatabase.transactionDao(), localDatabase.assetDao(), transaction, activeAsset, linkedTransaction, activeAssetOfLinkedTransaction)
            }
        } else {
            runBlocking {
                localDatabase.complexDao().cancelTransaction(localDatabase.transactionDao(), localDatabase.assetDao(), transaction, activeAsset)
            }
        }
    }

    fun getTransactionByID(transactionId: String): Transaction? {
        var transaction: Transaction?
        runBlocking {
            transaction = localDatabase.transactionDao().getTransactionByID(transactionId)
        }
        return  transaction
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
            transaction = dao.getTransactionsByCategoryId(category.categoryId).toMutableList()
        }
        return transaction
    }

    fun isCollidingTransactionExist(transaction: Transaction): Boolean {
        var result = false
        val dao = localDatabase.transactionDao()
        runBlocking {
            result = (dao.getCollidingTransaction(transaction.transactionDate, transaction.amount).isNotEmpty())
        }
        return result
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
            categories = if (transactionType!=null) dao.getCategories(transactionType) else dao.getAllCategories()
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
