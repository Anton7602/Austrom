package com.colleagues.austrom.database

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking





class LocalDatabaseProvider(private val activity: FragmentActivity) : IDatabaseProvider {
    private val localDatabase = LocalDatabase.getDatabase(activity.baseContext)

    override fun createNewUser(user: User): String? {
        TODO("Not yet implemented")
    }

    override fun updateUser(user: User) {
        TODO("Not yet implemented")
    }

    override fun deleteUser(user: User) {
        TODO("Not yet implemented")
    }

    override fun getUserByUserId(userId: String): User? {
        TODO("Not yet implemented")
    }

    override fun getUserByUsername(username: String): User? {
        TODO("Not yet implemented")
    }

    override fun getUserByEmail(email: String): User? {
        TODO("Not yet implemented")
    }

    override fun getUsersByBudget(budgetId: String): MutableMap<String, User> {
        TODO("Not yet implemented")
    }

    override fun createNewAsset(asset: Asset): String? {
        val dao = localDatabase.assetDao()
        asset.assetId = Asset.generateUniqueAssetKey(asset);
        activity.lifecycleScope.launch {
            dao.insertAsset(asset)
        }
        return asset.assetId
    }

    override fun updateAsset(asset: Asset) {
        TODO("Not yet implemented")
    }

    override fun deleteAsset(asset: Asset) {
        TODO("Not yet implemented")
    }

    override fun getAssetsOfUser(user: User): MutableMap<String, Asset> {
        val assetMap = mutableMapOf<String, Asset>()
        val dao = localDatabase.assetDao()
        runBlocking {
            val assets = dao.getAssetsOfUser(user.userId.toString())
            for (asset in assets) {
                assetMap[asset.assetId] = asset
            }
        }
        return assetMap
    }

    override fun getAssetsOfBudget(budget: Budget): MutableMap<String, Asset> {
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

    override fun createNewBudget(budget: Budget): String? {
        TODO("Not yet implemented")
    }

    override fun updateBudget(budget: Budget) {
        TODO("Not yet implemented")
    }

    override fun deleteBudget(budget: Budget) {
        TODO("Not yet implemented")
    }

    override fun getBudgetById(budgetId: String): Budget? {
        TODO("Not yet implemented")
    }

    override fun writeNewTransaction(transaction: Transaction): String? {
        val dao = localDatabase.transactionDao()
        transaction.transactionId = Transaction.generateUniqueTransactionKey(transaction);
        activity.lifecycleScope.launch {
            dao.insertTransaction(transaction)
        }
        return transaction.transactionId
    }

    override fun updateTransaction(transaction: Transaction) {
        TODO("Not yet implemented")
    }

    override fun deleteTransaction(transaction: Transaction) {
        TODO("Not yet implemented")
    }

    override fun getTransactionsOfUser(user: User): MutableList<Transaction> {
        val dao =  localDatabase.transactionDao()
        var transactions: MutableList<Transaction>
        runBlocking {
            transactions = dao.getTransactionsOfUser(user.userId.toString()).toMutableList()
        }
        return transactions
    }

    override fun getTransactionsOfBudget(budget: Budget): MutableList<Transaction> {
        val dao =  localDatabase.transactionDao()
        var transactions: MutableList<Transaction>
        runBlocking {
            transactions = dao.getTransactionsOfBudget().toMutableList()
        }
        return transactions
    }

    override fun getTransactionsOfAsset(asset: Asset): MutableList<Transaction> {
        val dao =  localDatabase.transactionDao()
        var transactions: MutableList<Transaction>
        runBlocking {
            transactions = dao.getTransactionsOfAsset(asset.assetId).toMutableList()
        }
        return transactions
    }

    override fun getCurrencies(): MutableMap<String, Currency> {
        TODO("Not yet implemented")
    }

}

@Dao
interface AssetDao {
    @Insert
    suspend fun insertAsset(asset: Asset)

    @Query("SELECT * FROM Asset WHERE Asset.userId=:userId")
    suspend fun getAssetsOfUser(userId: String): List<Asset>

    @Query("SELECT * FROM Asset")
    suspend fun getAssetsOfBudget(): List<Asset>

}

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM `Transaction` as Trn WHERE Trn.userId=:userId")
    suspend fun getTransactionsOfUser(userId: String): List<Transaction>

    @Query("SELECT * FROM `Transaction` as Trn WHERE Trn.sourceId=:assetId OR Trn.targetId=:assetId")
    suspend fun getTransactionsOfAsset(assetId: String): List<Transaction>

    @Query("SELECT * FROM `Transaction`")
    suspend fun getTransactionsOfBudget(): List<Transaction>
}