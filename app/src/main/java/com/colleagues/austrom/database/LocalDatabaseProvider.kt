package com.colleagues.austrom.database

import android.content.Context
import androidx.lifecycle.LiveData
import com.colleagues.austrom.AustromApplication.Companion.knownUsers
import com.colleagues.austrom.extensions.toInt
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.InvalidTransactionException
import com.colleagues.austrom.models.Invitation
import com.colleagues.austrom.models.Plan
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionFilter
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.models.TransactionValidationType
import com.colleagues.austrom.models.User
import com.colleagues.austrom.views.PeriodType
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class LocalDatabaseProvider(context: Context) {
    private val localDatabase = LocalDatabase.getDatabase(context)

    //region User
    fun writeNewUser(user: User) { runBlocking {localDatabase.userDao().insertUser(user) }}
    suspend fun writeNewUserAsync(user: User) { localDatabase.userDao().insertUser(user) }

    fun updateUser(user: User) { runBlocking { localDatabase.userDao().updateUser(user) }}
    suspend fun updateUserAsync(user: User) {  localDatabase.userDao().updateUser(user) }

    fun deleteUser(user: User) { runBlocking { localDatabase.userDao().deleteUser(user) }}
    suspend fun deleteUserAsync(user: User) { runBlocking { localDatabase.userDao().deleteUser(user) }}

    fun deleteAllUsers() { runBlocking { localDatabase.userDao().clearAllUsers() }}
    suspend fun deleteAllUsersAsync() { localDatabase.userDao().clearAllUsers() }

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
    //endregion

    //region Asset
    fun createNewAsset(asset: Asset): String {
        val dao = localDatabase.assetDao()
        //asset.assetId = Asset.generateUniqueAssetKey();
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

    suspend fun deleteAssetAsync(asset: Asset) { localDatabase.complexDao().deleteAssetWithTransactions(localDatabase.transactionDao(), localDatabase.assetDao(), asset) }
    fun deleteAsset(asset: Asset) {runBlocking { localDatabase.complexDao().deleteAssetWithTransactions(localDatabase.transactionDao(), localDatabase.assetDao(), asset) } }
    


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

    fun getAssetsOfBudget(): MutableMap<String, Asset> {
        val assetMap = mutableMapOf<String, Asset>()
        val dao = localDatabase.assetDao()
        runBlocking {
            val assets = dao.getAssetsOfBudget(knownUsers.values.map { l -> l.userId })
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
            val assets = dao.getAssetById(assetId)
            if (assets.isNotEmpty()) {
                asset = assets.first()
            }
        }
        return asset
    }

    fun getAssetsByAssetFilterAsync() : LiveData<List<Asset>> {
        return localDatabase.assetDao().getAssetsByFilter(knownUsers.values.map { l -> l.userId })
    }
    //endregion

    //region Transaction
    fun submitNewTransaction(transaction: Transaction, asset: Asset) {
        runBlocking {
            localDatabase.complexDao().submitTransaction(localDatabase.transactionDao(), localDatabase.assetDao(), transaction, asset)
        }
    }

    fun insertNewTransaction(transaction: Transaction) {
        runBlocking { localDatabase.transactionDao().insertTransaction(transaction) }
    }

    fun deleteTransaction(transaction: Transaction) {
        runBlocking { localDatabase.transactionDao().deleteTransaction(transaction) }
    }

    fun updateTransaction(transaction: Transaction) {
        val dao = localDatabase.transactionDao()
        runBlocking {
            dao.updateTransaction(transaction)
        }
    }

    fun cancelTransaction(transaction: Transaction) {
        val activeAsset = this.getAssetById(transaction.assetId) ?: throw InvalidTransactionException("Transaction cancellation failed. Asset is unknown!", TransactionValidationType.UNKNOWN_ASSET_INVALID)
        activeAsset.amount -= transaction.amount
        if (transaction.linkedTransactionId!=null) {
            val linkedTransaction = this.getTransactionByID(transaction.linkedTransactionId!!) ?: throw InvalidTransactionException("Transaction cancellation failed. Linked Transaction can't be found", TransactionValidationType.UNKNOWN_LINKED_TRANSACTION)
            val activeAssetOfLinkedTransaction = this.getAssetById(linkedTransaction.assetId) ?: throw InvalidTransactionException("Transaction cancellation failed. Asset of linked transaction is unknown!", TransactionValidationType.UNKNOWN_ASSET_INVALID)
            activeAssetOfLinkedTransaction.amount -= linkedTransaction.amount
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
            transactions = dao.getTransactionsOfUser(user.userId).toMutableList()
        }
        return transactions
    }

    fun getTransactionsOfActiveBudget(): MutableList<Transaction> {
        val dao =  localDatabase.transactionDao()
        var transactions: MutableList<Transaction>
        runBlocking {
            transactions = dao.getTransactionsOfBudget(knownUsers.values.map { l -> l.userId }).toMutableList()
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
            transaction = dao.getTransactionsByCategoryId(knownUsers.values.map { l -> l.userId }, category.categoryId).toMutableList()
        }
        return transaction
    }

    fun getTransactionsByTransactionFilterAsync(transactionFilter: TransactionFilter): LiveData<List<Transaction>> {
        return localDatabase.transactionDao().getTransactionsByCategoryAndDate(knownUsers.values.map { l -> l.userId }, transactionFilter.categories, transactionFilter.dateFrom!!.toInt(), transactionFilter.dateTo!!.toInt())
    }

    fun isCollidingTransactionExist(transaction: Transaction): Boolean {
        var result = false
        val dao = localDatabase.transactionDao()
        runBlocking {
            result = (dao.getCollidingTransaction(knownUsers.values.map { l -> l.userId }, transaction.transactionDate, transaction.amount).isNotEmpty())
        }
        return result
    }

    fun getUniqueTransactionNamesAsync(transactionType: TransactionType? = null): LiveData<List<String>> {
        return when (transactionType) {
            TransactionType.INCOME -> localDatabase.transactionDao().getUniqueIncomeNames(knownUsers.values.map { l -> l.userId })
            TransactionType.EXPENSE -> localDatabase.transactionDao().getUniqueExpenseNames(knownUsers.values.map { l -> l.userId })
            TransactionType.TRANSFER -> localDatabase.transactionDao().getUniqueTransactionNames(knownUsers.values.map { l -> l.userId })
            else -> localDatabase.transactionDao().getUniqueTransactionNames(knownUsers.values.map { l -> l.userId })
        }
    }

    fun getTransactionNameMostUsedCategory(transactionName: String): String? {
        var result: String?
        val dao = localDatabase.transactionDao()
        runBlocking {
            result = dao.getMostUsedCategoryOfTransactionName(transactionName)
        }
        return result
    }
    //endregion

    //region TransactionDetail

    fun writeNewTransactionDetail(transactionDetail: TransactionDetail) {
        //val dao = localDatabase.transactionDetailDao()
        //transactionDetail.transactionDetailId = TransactionDetail.generateUniqueTransactionKey();
        runBlocking {
            localDatabase.transactionDetailDao().insertTransactionDetail(transactionDetail)
        }
    }

    fun removeTransactionDetail(transactionDetail: TransactionDetail) {
        //TODO("Write Query for this One")
//        val dao = localDatabase.transactionDetailDao()
//        runBlocking {
//            dao.insertTransactionDetail(transactionDetail)
//        }
//        return transactionDetail.transactionDetailId
    }

    fun getTransactionDetailsOfTransaction(transaction: Transaction) : List<TransactionDetail> {
        val dao = localDatabase.transactionDetailDao()
        var transactionDetails: MutableList<TransactionDetail>
        runBlocking {
            transactionDetails = dao.getTransactionDetailsOfTransaction(transaction.transactionId).toMutableList()
        }
        return transactionDetails
    }

    fun getTransactionDetailById(transactionDetailId: String) : TransactionDetail? {
        val dao = localDatabase.transactionDetailDao()
        var transactionDetail: TransactionDetail? = null
        runBlocking {
            transactionDetail = dao.getTransactionDetailById(transactionDetailId)
        }
        return transactionDetail
    }

    fun removeTransactionDetailsOfTransaction(transaction: Transaction) {
        val dao = localDatabase.transactionDetailDao()
        runBlocking {
            dao.removeTransactionDetailsOfTransaction(transaction.transactionId)
        }
    }

    fun getUniqueTransactionDetailsNamesAsync(): LiveData<List<String>> { return localDatabase.transactionDetailDao().getUniqueTransactionDetailsNames() }
    //endregion

    //region Currency
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
    //endregion

    //region Invitation
    fun insertInvitation(invitation: Invitation) {
        runBlocking {
            localDatabase.invitationDao().insertInvitation(invitation)
        }
    }

    fun getInvitations(): List<Invitation> {
        val dao = localDatabase.invitationDao()
        var invitations = listOf<Invitation>()
        runBlocking {
            invitations = dao.getAllInvitations()
        }
        return invitations
    }

    fun getInvitationsOfBudget(budgetId: String): List<Invitation> {
        val dao = localDatabase.invitationDao()
        var invitations: List<Invitation>
        runBlocking {
            invitations = dao.getAllInvitationsOfBudget(budgetId)
        }
        return invitations
    }

    fun recallInvitationToUser(userId: String) {
        runBlocking { localDatabase.invitationDao().deleteInvitationByUserId(userId) }
    }

    fun getInvitationByUserId(userId: String): Invitation? {
        val invitation: Invitation?
        runBlocking {
            invitation = localDatabase.invitationDao().getInvitationById(userId)
        }
        return invitation
    }
    //endregion

    //region Plan
    fun insertPlan(plan: Plan) {
        runBlocking { localDatabase.planDao().insertPlan(plan) }
    }

    fun getPlan(date: LocalDate, periodType: PeriodType, categoryType: Category): Plan? {
        var plan: Plan?  = null
        runBlocking {
            plan = localDatabase.planDao().getPlan(date.toInt().toString(), periodType.toString(), categoryType.categoryId)
        }
        return plan
    }
    //endregion

    //region Category
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
            categories = if (transactionType!=null) dao.getCategories(knownUsers.values.map { l -> l.userId }, transactionType) else dao.getAllCategories(knownUsers.values.map { l -> l.userId })
        }
        return categories
    }

    fun getCategoryById(categoryId: String) : Category? {
        val dao = localDatabase.categoryDao()
        var category: Category? = null
        runBlocking {
            val categories = dao.getCategoryById(categoryId)
            if (categories.isNotEmpty()) {
                category = categories.first()
            }
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
    //endregion
}
