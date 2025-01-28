package com.colleagues.austrom.managers

import android.content.Context
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.intAfterLastPipe
import com.colleagues.austrom.extensions.substringBeforeLastPipe
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Currency
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SyncManager(val context: Context, val localDBProvider: LocalDatabaseProvider, val remoteDBProvider: IRemoteDatabaseProvider) {
    val budget = remoteDBProvider.getBudgetById(AustromApplication.appUser?.activeBudgetId.toString())

    fun sync() {
        syncCurrenciesRemoteToLocal()
    }

    private fun syncCurrenciesRemoteToLocal() {
        if (remoteDBProvider is FirebaseDatabaseProvider) setCurrenciesListener(remoteDBProvider)
    }

    private fun syncAssetsRemoteToLocal() {
        if (remoteDBProvider is FirebaseDatabaseProvider) setAssetListener(remoteDBProvider)

    }

    private fun syncTransactionsRemoteToLocal() {
        if (remoteDBProvider is FirebaseDatabaseProvider) setTransactionListener(remoteDBProvider)

    }

    private fun syncTransactionsDetailsRemoteToLocal() {
        if (remoteDBProvider is FirebaseDatabaseProvider) setTransactionDetailListener(remoteDBProvider)
    }

    private fun setCurrenciesListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        firebaseDatabaseProvider.setCurrenciesListener(
        object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currencies = mutableMapOf<String, Currency>()
                for (snapshotItem in dataSnapshot.children) {
                    val currency = snapshotItem.getValue(Currency::class.java)
                    if (currency!=null) {
                        currencies[currency.code] = currency
                    }
                }
                AustromApplication.activeCurrencies = Currency.switchRatesToNewBaseCurrency(Currency.localizeCurrencyNames(currencies, context), AustromApplication.appUser?.baseCurrencyCode)
                localDBProvider.deleteAllCurrencies()
                currencies.forEach { currency ->
                    localDBProvider.writeCurrency(currency.value)
                }
                syncAssetsRemoteToLocal()
            }
            override fun onCancelled(databaseError: DatabaseError) { Log.w("Debug", "loadPost:onCancelled", databaseError.toException()) }
        })
    }

    private fun setAssetListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        if (budget==null) return
        firebaseDatabaseProvider.setAssetListener(
            object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val encryptionManager = EncryptionManager()
                for (snapshotItem in dataSnapshot.children) {
                    val localAsset = localDBProvider.getAssetById(snapshotItem.key.toString())
                    if (localAsset!=null && snapshotItem.value=="-") {
                        localAsset.delete(localDBProvider)
                        if (AustromApplication.activeAssets.containsKey(localAsset.assetId)) {
                            AustromApplication.activeAssets.remove(localAsset.assetId)
                        }
                    } else {
                        val asset = encryptionManager.decryptAsset(snapshotItem.getValue(String::class.java).toString(), encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId))
                        if (localAsset!=null) {
                            localDBProvider.updateAsset(asset)
                        } else {
                            localDBProvider.createNewAsset(asset)
                        }
                        AustromApplication.activeAssets[asset.assetId] = asset
                    }
                }
                syncTransactionsRemoteToLocal()
            }

            override fun onCancelled(databaseError: DatabaseError) { Log.w("Debug", "loadPost:onCancelled", databaseError.toException()) }
        }, budget)
    }

    private fun setTransactionListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        if (budget==null) return
        firebaseDatabaseProvider.setTransactionListener(
        object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val encryptionManager = EncryptionManager()
                for (snapshotItem in dataSnapshot.children) {
                    val localTransaction = localDBProvider.getTransactionByID(snapshotItem.key.toString())
                    if (localTransaction==null) {
                        if (snapshotItem.value!="-") {
                            val transaction = encryptionManager.decryptTransaction(snapshotItem.getValue(String::class.java).substringBeforeLastPipe().toString(),
                                encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId))
                            val asset = AustromApplication.activeAssets[transaction.assetId]
                            if (asset!=null) {
                                localDBProvider.insertNewTransaction(transaction)
                            }
                            //TODO("What if asset of this transaction doesn't exist?")
                        }
                    } else {
                        if (snapshotItem.value =="-") {
                            localDBProvider.deleteTransaction(localTransaction)
                        } else {
                            val remoteVersion = snapshotItem.getValue(String::class.java).intAfterLastPipe()
                            if (remoteVersion!=null && remoteVersion>localTransaction.version) {
                                val transaction = encryptionManager.decryptTransaction(snapshotItem.getValue(String::class.java).substringBeforeLastPipe().toString(),
                                    encryptionManager.convertStringToSecretKey(AustromApplication.appUser!!.tokenId))
                                transaction.version = remoteVersion
                                localDBProvider.updateTransaction(transaction)
                            }
                        }
                    }
                }
                syncTransactionsDetailsRemoteToLocal()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Debug", "loadPost:onCancelled", databaseError.toException())
            }
        }, budget)
    }

    private fun setTransactionDetailListener(firebaseDatabaseProvider: FirebaseDatabaseProvider) {
        if (budget==null) return
        firebaseDatabaseProvider.setTransactionDetailListener(
            object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val encryptionManager = EncryptionManager()
                for (snapshotItem in dataSnapshot.children) {
                    val localTransactionDetail = localDBProvider.getTransactionDetailById(snapshotItem.key.toString())
                    if (localTransactionDetail==null) {
                        if (snapshotItem.value!="-") {
                            val transactionDetail = encryptionManager.decryptTransactionDetail(snapshotItem.getValue(String::class.java).toString(), encryptionManager.convertStringToSecretKey(
                                AustromApplication.appUser!!.tokenId))
                            localDBProvider.writeNewTransactionDetail(transactionDetail)
                        }
                    } else {
                        if (snapshotItem.value =="-") {
                            localDBProvider.removeTransactionDetail(localTransactionDetail)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Debug", "loadPost:onCancelled", databaseError.toException())
            }
        }, budget)
    }
}

@Entity(tableName = "change_log")
data class AssetChangeLog(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val entityId: Long,
    val action: String,
    val currentVersion: Int
)

