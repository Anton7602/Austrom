package com.colleagues.austrom.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.User
import java.time.LocalDate
import java.time.ZoneId

@Database(entities = [Asset::class, Transaction::class, TransactionDetail::class, User::class, Currency::class, Category::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LocalDatabase: RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionDetailDao(): TransactionDetailsDao
    abstract fun userDao(): UserDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getDatabase(context: Context): LocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "local_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM User")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM User WHERE User.userId=:userId")
    suspend fun getUserByUserId(userId: String): List<User>

    @Query("SELECT * FROM User WHERE User.username=:username")
    suspend fun getUserByUsername(username: String): List<User>

}

@Dao
interface AssetDao {
    @Insert
    suspend fun insertAsset(asset: Asset)

    @Update
    suspend fun updateAsset(asset: Asset)

    @Delete
    suspend fun deleteAsset(asset: Asset)

    @Query("SELECT * FROM Asset WHERE Asset.userId=:userId")
    suspend fun getAssetsOfUser(userId: String): List<Asset>

    @Query("SELECT * FROM Asset")
    suspend fun getAssetsOfBudget(): List<Asset>
}

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM `Transaction` as Trn WHERE Trn.userId=:userId")
    suspend fun getTransactionsOfUser(userId: String): List<Transaction>

    @Query("SELECT * FROM `Transaction` as Trn WHERE Trn.sourceId=:assetId OR Trn.targetId=:assetId")
    suspend fun getTransactionsOfAsset(assetId: String): List<Transaction>

    @Query("SELECT * FROM `Transaction`")
    suspend fun getTransactionsOfBudget(): List<Transaction>
}

@Dao
interface TransactionDetailsDao {
    @Insert
    suspend fun insertTransactionDetail(transactionDetail: TransactionDetail)

    @Update
    suspend fun updateTransactionDetail(transactionDetail: TransactionDetail)

    @Delete
    suspend fun deleteTransactionDetail(transactionDetail: TransactionDetail)

    @Query ("SELECT * FROM TransactionDetail as Trd WHERE Trd.transactionId =:transactionId")
    suspend fun getTransactionDetailsOfTransaction(transactionId: String): List<TransactionDetail>
}

@Dao
interface CurrencyDao {
    @Insert
    suspend fun insertCurrency(currency: Currency)

    @Update
    suspend fun updateCurrency(currency: Currency)

    @Delete
    suspend fun deleteCurrency(currency: Currency)

    @Query("SELECT * FROM CURRENCY")
    suspend fun getAllCurrencies(): List<Currency>

    @Query("DELETE FROM Currency")
    suspend fun deleteAllCurrencies()
}

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM Category")
    suspend fun getAllCategories(): List<Category>
}

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        return date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toLocalDate(timestamp: Long?): LocalDate? {
        return timestamp?.let {
            LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
        }
    }
}