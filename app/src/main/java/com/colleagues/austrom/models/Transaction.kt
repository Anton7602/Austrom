package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.serialize
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Entity(foreignKeys = [ForeignKey(entity = Asset::class,
    parentColumns = ["assetId"],
    childColumns = ["assetId"],
    onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = Category::class,
        parentColumns = ["categoryId"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["assetId"]), Index(value = ["categoryId"]), Index(value = ["userId"])])
class Transaction(val assetId: String, val amount: Double, var categoryId: String, val transactionDate: LocalDate, val transactionName: String, var comment: String? = null,
    @PrimaryKey(autoGenerate = false)
    var transactionId: String = generateUniqueTransactionKey(),
    var userId: String = AustromApplication.appUser!!.userId,
    var linkedTransactionId: String? = null,
    var isPrivate: Boolean = false,
    var version: Int = 1 )  {

    fun transactionType(): TransactionType { return if (linkedTransactionId!=null) TransactionType.TRANSFER else if (amount<0) TransactionType.EXPENSE else TransactionType.INCOME }
    fun sumOfTransactionDetailsAmounts(localDBProvider: LocalDatabaseProvider): Double { return localDBProvider.getTransactionDetailsOfTransaction(this).sumOf {it.cost} }
    fun serialize(): String { return "$transactionId,$amount,$categoryId,$assetId,${transactionDate.serialize()},$transactionName,$comment,$userId,$linkedTransactionId,$isPrivate" }

    fun linkTo(transaction: Transaction) {
        if (transactionId==transaction.transactionId) throw IllegalArgumentException("Link can not be pointing on itself")
        if (linkedTransactionId!=null || transaction.linkedTransactionId!=null) throw IllegalArgumentException("One of transaction is already in linked state")
        this.linkTo(transaction.transactionId)
        transaction.linkTo(this.transactionId)
    }

    private fun linkTo(transactionId: String?) { this.linkedTransactionId = transactionId }
    private fun clearLink() { this.linkedTransactionId = null}

    fun breakLink(dbProvider: LocalDatabaseProvider) {
        if (linkedTransactionId == null) return
        dbProvider.getTransactionByID(linkedTransactionId!!)?.clearLink()
        this.clearLink()
    }

    fun update(localDBProvider: LocalDatabaseProvider, remoteDBProvider: FirebaseDatabaseProvider? = null) {
        // TODO("Validate Changes To Transaction")
        localDBProvider.updateTransaction(this)
        if (remoteDBProvider != null && AustromApplication.appUser?.activeBudgetId != null) {
            val currentBudget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
            if (currentBudget != null) remoteDBProvider.updateTransaction(this, currentBudget)
        }
    }

    fun submit(localDBProvider: LocalDatabaseProvider, remoteDBProvider: FirebaseDatabaseProvider? = null) {
        if (this.validate()==TransactionValidationType.VALID) {
            val involvedAsset = AustromApplication.activeAssets[assetId] ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_ASSET_INVALID )
            involvedAsset.amount += this.amount
            localDBProvider.submitNewTransaction(this, involvedAsset)
            if (remoteDBProvider != null && AustromApplication.appUser?.activeBudgetId != null) {
                val currentBudget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
                if (currentBudget != null) {
                    remoteDBProvider.createNewTransaction(this, currentBudget)
                }
            }
        } else throw InvalidTransactionException(this.validate())
    }

    fun cancel(dbProvider: LocalDatabaseProvider, remoteDBProvider: FirebaseDatabaseProvider? = null) {
        when (this.transactionType()) {
            TransactionType.TRANSFER -> {
                val involvedAsset = AustromApplication.activeAssets[assetId] ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_ASSET_INVALID)
                if (linkedTransactionId==null) throw InvalidTransactionException(TransactionValidationType.UNKNOWN_LINKED_TRANSACTION)
                val linkedTransaction = dbProvider.getTransactionByID(this.linkedTransactionId!!) ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_LINKED_TRANSACTION)
                val involvedAssetOfLinkedTransaction = AustromApplication.activeAssets[linkedTransaction.assetId] ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_ASSET_INVALID)
                involvedAsset.amount-=this.amount
                involvedAssetOfLinkedTransaction.amount-=linkedTransaction.amount
                dbProvider.cancelTransaction(this)
                if (remoteDBProvider!= null && AustromApplication.appUser?.activeBudgetId!=null) {
                    val currentBudget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
                    if (currentBudget!=null) {
                        remoteDBProvider.deleteTransaction(this, currentBudget)
                        remoteDBProvider.deleteTransaction(linkedTransaction, currentBudget)
                    }
                }
            }
            else -> {
                val involvedAsset = AustromApplication.activeAssets[assetId] ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_ASSET_INVALID)
                involvedAsset.amount-=this.amount
                dbProvider.cancelTransaction(this)
                if (remoteDBProvider!= null && AustromApplication.appUser?.activeBudgetId!=null) {
                    val currentBudget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
                    if (currentBudget!=null) {
                        remoteDBProvider.deleteTransaction(this, currentBudget)
                    }
                }
            }
        }
    }

    fun addDetail(transactionDetail: TransactionDetail, dbProvider: LocalDatabaseProvider, remoteDBProvider: FirebaseDatabaseProvider? = null) {
        dbProvider.writeNewTransactionDetail(transactionDetail)
        if (remoteDBProvider!=null) {
            val currentBudget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
            if (currentBudget!=null) {
                remoteDBProvider.createNewTransactionDetail(transactionDetail, currentBudget)
            }
        }
        //TODO("Validate and finish")
    }

    fun isValid(): Boolean { return (this.validate()==TransactionValidationType.VALID)  }

    fun isColliding(dbProvider: LocalDatabaseProvider): Boolean {return dbProvider.isCollidingTransactionExist(this)}

    fun validate(): TransactionValidationType {
        if (amount==0.0) return TransactionValidationType.AMOUNT_INVALID
        if (AustromApplication.activeAssets[assetId]==null) return TransactionValidationType.UNKNOWN_ASSET_INVALID
        when (transactionType()) {
            TransactionType.INCOME -> if (AustromApplication.activeIncomeCategories[categoryId]==null) return TransactionValidationType.UNKNOWN_CATEGORY_INVALID
            TransactionType.EXPENSE -> if (AustromApplication.activeExpenseCategories[categoryId]==null) return TransactionValidationType.UNKNOWN_CATEGORY_INVALID
            else -> {}
        }
        return TransactionValidationType.VALID
    }

    fun validate(dbProvider: LocalDatabaseProvider) : TransactionValidationType {
        return if (linkedTransactionId==null || dbProvider.getTransactionByID(linkedTransactionId!!)!=null) this.validate() else TransactionValidationType.UNKNOWN_LINKED_TRANSACTION
    }

    companion object{
        fun groupTransactionsByDate(transactions: MutableList<Transaction>) : MutableMap<LocalDate, MutableList<Transaction>> {
            transactions.sortByDescending { it.transactionDate }
            val groupedTransactions = mutableMapOf<LocalDate, MutableList<Transaction>>()
            for (transaction in transactions){
                if (!groupedTransactions.containsKey(transaction.transactionDate)) {
                    groupedTransactions[transaction.transactionDate] = mutableListOf()
                }
                groupedTransactions[transaction.transactionDate]!!.add(transaction)
            }
            return  groupedTransactions
        }

        fun generateUniqueTransactionKey() : String {
            return UUID.randomUUID().toString()
        }

        fun deserialize(serializedTransaction: String): Transaction {
            val dataParts = serializedTransaction.split(",")
            return Transaction(
                transactionId = dataParts[0] ,
                amount = dataParts[1].toDouble(),
                categoryId = dataParts[2],
                assetId = dataParts[3],
                transactionDate = LocalDate.parse(dataParts[4], DateTimeFormatter.ISO_LOCAL_DATE) ,
                transactionName = dataParts[5],
                comment = if (dataParts[6]=="null") null else dataParts[5],
                userId = dataParts[7] ,
                linkedTransactionId = if (dataParts[8]=="null") null else dataParts[8] ,
                isPrivate=dataParts[9].toBoolean()
            )
        }
    }
}

enum class TransactionType(val transactionTypeNameId: Int = R.string.unresolved, val transactionTypeDescriptionResourceId: Int = R.string.unresolved, val transactionTypeIconResource: Int = R.drawable.ic_placeholder_icon) {
    INCOME(R.string.income, R.string.income_desc, R.drawable.ic_transactiontype_income_temp),
    EXPENSE(R.string.expense, R.string.expense_desc, R.drawable.ic_transactiontype_expense_temp),
    TRANSFER(R.string.transfer, R.string.transfer_desc, R.drawable.ic_transactiontype_transfer_temp)
}

enum class TransactionValidationType{
    VALID, UNKNOWN_ASSET_INVALID, UNKNOWN_CATEGORY_INVALID, UNKNOWN_LINKED_TRANSACTION, AMOUNT_INVALID
}

class InvalidTransactionException(message: String, validationType: TransactionValidationType) : Exception(message) {
    constructor(validationType: TransactionValidationType): this(when(validationType) {
        TransactionValidationType.VALID -> "CRITICAL ERROR!!! Transaction type valid but InvalidTransactionException thrown."
        TransactionValidationType.UNKNOWN_ASSET_INVALID -> "Asset of provided transaction was not recognized"
        TransactionValidationType.UNKNOWN_CATEGORY_INVALID -> "Category of provided transaction was not recognized"
        TransactionValidationType.UNKNOWN_LINKED_TRANSACTION -> "Linked Transaction of provided transaction is not recognized"
        TransactionValidationType.AMOUNT_INVALID -> "Transaction Amount cannot be equal to zero"
    }, validationType)
}

class TransactionFilter(val categories: MutableList<String>, val dateFrom: LocalDate?, val dateTo: LocalDate?)