package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.equalTo
import com.colleagues.austrom.extensions.serialize
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.models.Transaction.Companion.generateUniqueTransactionKey
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
class Transaction(val assetId: String, var amount: Double, var categoryId: String, var transactionDate: LocalDate, var transactionName: String, var comment: String? = null,
    @PrimaryKey(autoGenerate = false)
    var transactionId: String = generateUniqueTransactionKey(),
    var userId: String = AustromApplication.appUser!!.userId,
    var linkedTransactionId: String? = null,
    var isPrivate: Boolean = false,
    var version: Int = 0 )  {

    override fun equals(other: Any?): Boolean {
        if (other !is Transaction) return false
        return (this.transactionId==other.transactionId)
    }

    override fun hashCode(): Int {
        return transactionId.hashCode()
    }

    fun isCollidingWith(transaction: Transaction?): Boolean { return (transaction!=null && assetId==transaction.assetId && amount.equalTo(transaction.amount) && transactionDate==transaction.transactionDate && transactionName==transaction.transactionName) }
    fun transactionType(): TransactionType { return if (linkedTransactionId!=null) TransactionType.TRANSFER else if (amount<0) TransactionType.EXPENSE else TransactionType.INCOME }
    fun sumOfTransactionDetailsAmounts(localDBProvider: LocalDatabaseProvider): Double { return localDBProvider.getTransactionDetailsOfTransaction(this).sumOf {it.cost} }
    fun serialize(): String { return "$transactionId,$amount,$categoryId,$assetId,${transactionDate.serialize()},$transactionName,$comment,$userId,$linkedTransactionId,$isPrivate" }
    fun toCSVFormat(): String {
        val activeAsset = AustromApplication.activeAssets[assetId] ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_ASSET_INVALID)
        val category = AustromApplication.activeCategories[categoryId] ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_CATEGORY_INVALID)
        var originalAmount = this.amount
        if (activeAsset.currencyCode!=AustromApplication.appUser!!.baseCurrencyCode) {
            originalAmount *= (AustromApplication.activeCurrencies[activeAsset.currencyCode]?.exchangeRate ?: 1.0)
        }
        return "${transactionName},${transactionDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))},${amount},${activeAsset.currencyCode},${originalAmount},${AustromApplication.appUser!!.baseCurrencyCode},${activeAsset.assetName},${category.name},${AustromApplication.appUser!!.username},${comment}"
    }

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
        if (this.validate() == TransactionValidationType.VALID) {
            this.version++
            localDBProvider.updateTransaction(this)
            if (remoteDBProvider != null && AustromApplication.appUser?.activeBudgetId != null) {
                val currentBudget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
                if (currentBudget != null) remoteDBProvider.updateTransaction(this, currentBudget)
            }
        } else throw InvalidTransactionException(this.validate())
    }

    fun submit(localDBProvider: LocalDatabaseProvider, remoteDBProvider: FirebaseDatabaseProvider? = null) {
        if (this.validate()==TransactionValidationType.VALID) {
            val involvedAsset = AustromApplication.activeAssets[assetId] ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_ASSET_INVALID )
            involvedAsset.amount += this.amount
            localDBProvider.submitNewTransaction(this, involvedAsset)
            if (remoteDBProvider != null && AustromApplication.appUser?.activeBudgetId != null) {
                val currentBudget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId!!)
                if (currentBudget != null) {
                    remoteDBProvider.conductTransaction(this, currentBudget)
                }
            }
        } else throw InvalidTransactionException(this.validate())
    }

    fun removeTransactionDetails(localDBProvider: LocalDatabaseProvider, remoteDBProvider: FirebaseDatabaseProvider?) {
        if (remoteDBProvider!=null) {
            val transactionDetails = localDBProvider.getTransactionDetailsOfTransaction(this)
            transactionDetails.forEach { transactionDetail -> if (AustromApplication.activeBudget!=null) remoteDBProvider.deleteTransactionDetail(AustromApplication.activeBudget!!, transactionDetail) }
        }
        localDBProvider.removeTransactionDetailsOfTransaction(this)
    }

    fun cancel(dbProvider: LocalDatabaseProvider, remoteDBProvider: FirebaseDatabaseProvider? = null) {
        removeTransactionDetails(dbProvider, remoteDBProvider)
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
                        remoteDBProvider.cancelTransaction(this, currentBudget, linkedTransaction)
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
                        remoteDBProvider.cancelTransaction(this, currentBudget)
                    }
                }
            }
        }
    }

    fun addDetail(transactionDetail: TransactionDetail, dbProvider: LocalDatabaseProvider, remoteDBProvider: FirebaseDatabaseProvider? = null) {
        dbProvider.writeNewTransactionDetail(transactionDetail)
        if (remoteDBProvider!=null) {
            val currentBudget = remoteDBProvider.getBudgetById(AustromApplication.appUser!!.activeBudgetId.toString())
            if (currentBudget!=null) {
                remoteDBProvider.insertTransactionDetail(transactionDetail, currentBudget)
            }
        }
        //TODO("Validate and finish")
    }

    fun amountInBaseCurrency(): Double {
        val transactionsAsset = AustromApplication.activeAssets[assetId] ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_ASSET_INVALID)
        return if (transactionsAsset.currencyCode==AustromApplication.appUser!!.baseCurrencyCode) amount else amount/(AustromApplication.activeCurrencies[transactionsAsset.currencyCode]?.exchangeRate ?: 1.0)
    }

    fun isValid(): Boolean { return (this.validate()==TransactionValidationType.VALID)  }

    fun isColliding(dbProvider: LocalDatabaseProvider): Boolean {return dbProvider.isCollidingTransactionExist(this)}

    fun validate(): TransactionValidationType {
        if (amount==0.0) return TransactionValidationType.AMOUNT_INVALID
        if (AustromApplication.activeAssets[assetId]==null) return TransactionValidationType.UNKNOWN_ASSET_INVALID
        if (transactionType()==TransactionType.TRANSFER) {
            if (categoryId!=AustromApplication.activeCategories.values.firstOrNull { category -> category.transactionType==TransactionType.TRANSFER }?.categoryId) {
                return TransactionValidationType.UNKNOWN_CATEGORY_INVALID
            }
        } else if (amount>0) {
            if (AustromApplication.activeCategories.filter { l -> l.value.transactionType==TransactionType.INCOME }[categoryId]==null) return TransactionValidationType.UNKNOWN_CATEGORY_INVALID
        } else {
            if (AustromApplication.activeCategories.filter { l -> l.value.transactionType==TransactionType.EXPENSE }[categoryId]==null) return TransactionValidationType.UNKNOWN_CATEGORY_INVALID
        }

        return TransactionValidationType.VALID
    }

    fun validate(dbProvider: LocalDatabaseProvider) : TransactionValidationType {
        return if (linkedTransactionId==null || dbProvider.getTransactionByID(linkedTransactionId!!)!=null) this.validate() else TransactionValidationType.UNKNOWN_LINKED_TRANSACTION
    }

    companion object{
        fun getSumOfTransactions(transactions: List<Transaction>): Double {
            var sum = 0.0
            transactions.forEach {
                val transactionCurrency = AustromApplication.activeAssets[it.assetId]?.currencyCode ?: throw InvalidTransactionException(TransactionValidationType.AMOUNT_INVALID)
                sum+=if (transactionCurrency==AustromApplication.appUser!!.baseCurrencyCode) it.amount else it.amount/(AustromApplication.activeCurrencies[transactionCurrency]?.exchangeRate ?: 1.0)
            }
            return sum
        }

        fun groupTransactionsBy(transactions: MutableList<Transaction>, groupByType: TransactionGroupByType, sortByAmount: Boolean = false): MutableMap<String, MutableList<Transaction>> {
            val groupedTransactions =  when(groupByType) {
                TransactionGroupByType.BY_DATE -> groupTransactionsByDate(transactions)
                TransactionGroupByType.BY_CATEGORY -> groupTransactionsByCategory(transactions)
                TransactionGroupByType.BY_NAME -> groupTransactionsByName(transactions)
            }


            return if (!sortByAmount) groupedTransactions else groupedTransactions.entries.sortedBy { it.value.sumOf { it.amount } }.associate { it.key to it.value }.toMutableMap()
        }

        private fun groupTransactionsByDate(transactions: MutableList<Transaction>) : MutableMap<String, MutableList<Transaction>> {
            transactions.sortByDescending { it.transactionDate }
            val groupedTransactions = mutableMapOf<String, MutableList<Transaction>>()
            for (transaction in transactions){
                if (!groupedTransactions.containsKey(transaction.transactionDate.toDayOfWeekAndShortDateFormat())) {
                    groupedTransactions[transaction.transactionDate.toDayOfWeekAndShortDateFormat()] = mutableListOf()
                }
                groupedTransactions[transaction.transactionDate.toDayOfWeekAndShortDateFormat()]!!.add(transaction)
            }
            return  groupedTransactions
        }

        private fun groupTransactionsByCategory(transactions: MutableList<Transaction>) : MutableMap<String, MutableList<Transaction>> {
            //transactions.sortBy { it.categoryId }
            val groupedTransactions = mutableMapOf<String, MutableList<Transaction>>()
            for (transaction in transactions){
                if (!groupedTransactions.containsKey(AustromApplication.activeCategories[transaction.categoryId]!!.name)) {
                    groupedTransactions[AustromApplication.activeCategories[transaction.categoryId]!!.name] = mutableListOf()
                }
                groupedTransactions[AustromApplication.activeCategories[transaction.categoryId]!!.name]!!.add(transaction)
            }
            return  groupedTransactions
        }

        private fun groupTransactionsByName(transactions: MutableList<Transaction>) : MutableMap<String, MutableList<Transaction>> {
            transactions.sortBy { it.transactionName }
            val groupedTransactions = mutableMapOf<String, MutableList<Transaction>>()
            for (transaction in transactions){
                if (!groupedTransactions.containsKey(transaction.transactionName)) {
                    groupedTransactions[transaction.transactionName] = mutableListOf()
                }
                groupedTransactions[transaction.transactionName]!!.add(transaction)
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
                comment = if (dataParts[6]=="null") null else dataParts[6],
                userId = dataParts[7] ,
                linkedTransactionId = if (dataParts[8]=="null") null else dataParts[8] ,
                isPrivate=dataParts[9].toBoolean()
            )
        }
    }
}

enum class TransactionType(val transactionTypeNameId: Int = R.string.unresolved, val transactionTypeDescriptionResourceId: Int = R.string.unresolved, val transactionTypeIconResource: Int = R.drawable.ic_placeholder_icon) {
    EXPENSE(R.string.expense, R.string.expense_desc, R.drawable.ic_transactiontype_expense_temp),
    INCOME(R.string.income, R.string.income_desc, R.drawable.ic_transactiontype_income_temp),
    TRANSFER(R.string.transfer, R.string.transfer_desc, R.drawable.ic_transactiontype_transfer_temp)
}
enum class TransactionValidationType{  VALID, UNKNOWN_ASSET_INVALID, UNKNOWN_CATEGORY_INVALID, UNKNOWN_LINKED_TRANSACTION, AMOUNT_INVALID }
class InvalidTransactionException(message: String, validationType: TransactionValidationType) : Exception(message) {
    constructor(validationType: TransactionValidationType): this(when(validationType) {
        TransactionValidationType.VALID -> "CRITICAL ERROR!!! Transaction type valid but InvalidTransactionException thrown."
        TransactionValidationType.UNKNOWN_ASSET_INVALID -> "Asset of provided transaction was not recognized"
        TransactionValidationType.UNKNOWN_CATEGORY_INVALID -> "Category of provided transaction was not recognized"
        TransactionValidationType.UNKNOWN_LINKED_TRANSACTION -> "Linked Transaction of provided transaction is not recognized"
        TransactionValidationType.AMOUNT_INVALID -> "Transaction Amount cannot be equal to zero"
    }, validationType)
}

data class TransactionFilter(val categories: MutableList<String>,val assets: MutableList<String>, val names: MutableList<String>, var dateFrom: LocalDate?, var dateTo: LocalDate?)
data class TransactionWithDetails(val assetId: String, var amount: Double, var categoryId: String, var transactionDate: LocalDate, var transactionName: String, var comment: String? = null,
                                  var transactionId: String = generateUniqueTransactionKey(), var userId: String = AustromApplication.appUser!!.userId, var linkedTransactionId: String? = null,
                                  var isPrivate: Boolean = false, var version: Int = 0, val name: String?, val cost: Double?, val quantity: Double? = null, val typeOfQuantity: QuantityUnit? = null,
                                  val categoryName: String? = null, var transactionDetailId: String?
)

enum class TransactionGroupByType{ BY_DATE, BY_CATEGORY, BY_NAME }
