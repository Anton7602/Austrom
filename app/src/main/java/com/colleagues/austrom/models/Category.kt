package com.colleagues.austrom.models

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.managers.Icon
import com.colleagues.austrom.managers.IconManager
import java.util.UUID

@Entity
class Category(var name: String,
               var imgReference: Icon,
               val transactionType: TransactionType,
               @PrimaryKey(autoGenerate = false)
               val categoryId: String,
    var userId: String = AustromApplication.appUser?.userId.toString(),
    var type: String? = null) {

    constructor(name: String, imgReference: Icon, transactionType: TransactionType): this(name, imgReference, transactionType, generateCategoryId())
    fun serialize(): String { return "$categoryId,$name,${imgReference.resourceId},${transactionType},$userId"}

    override fun toString(): String {
        return this.name
    }

    override fun equals(other: Any?): Boolean {
        val category = other as Category
        return (category.name==name && category.type==type && category.transactionType == transactionType)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + transactionType.hashCode()
        return result
    }

    companion object{
        fun generateCategoryId(): String { return UUID.randomUUID().toString() }

        fun deserialize(serializedCategory: String): Category {
            val dataParts = serializedCategory.split(",")
            return Category(
                categoryId = dataParts[0],
                name = dataParts[1],
                imgReference = IconManager().getIconByResourceId(dataParts[2].toInt()) ?: Icon.I0,
                transactionType = when (dataParts[3]) {
                    "EXPENSE" -> TransactionType.EXPENSE
                    "INCOME" -> TransactionType.INCOME
                    "TRANSFER" -> TransactionType.TRANSFER
                    else -> TransactionType.EXPENSE
                },
                userId = dataParts[4],
                type = null
            )
        }

        var defaultExpenseCategories : List<Category> = listOf(
            Category("Food",  Icon.I7, TransactionType.EXPENSE, generateCategoryId()),
            Category("Clothes", Icon.I3, TransactionType.EXPENSE, generateCategoryId()),
            Category("Health", Icon.I8, TransactionType.EXPENSE, generateCategoryId()),
            Category("House", Icon.I11, TransactionType.EXPENSE, generateCategoryId()),
            Category("Transport", Icon.I15, TransactionType.EXPENSE, generateCategoryId()),
            Category("Travel", Icon.I16, TransactionType.EXPENSE, generateCategoryId()),
            Category("Subscriptions", Icon.I13, TransactionType.EXPENSE, generateCategoryId()),
            Category("Entertainment", Icon.I5, TransactionType.EXPENSE, generateCategoryId()),
            Category("Sport", Icon.I12, TransactionType.EXPENSE, generateCategoryId()),
            Category("Beauty", Icon.I1, TransactionType.EXPENSE, generateCategoryId()),
            Category("Presents", Icon.I10, TransactionType.EXPENSE, generateCategoryId()),
            Category("Education", Icon.I4, TransactionType.EXPENSE, generateCategoryId()),
            Category("Equipment", Icon.I6, TransactionType.EXPENSE, generateCategoryId()),
            Category("Other", Icon.I9, TransactionType.EXPENSE, generateCategoryId()),
        )

        var defaultIncomeCategories : List<Category> = listOf(
            Category("Wages", Icon.I62, TransactionType.INCOME, generateCategoryId()),
            Category("Cashback", Icon.I2, TransactionType.INCOME, generateCategoryId()),
        )

        var defaultTransferCategories : List<Category> = listOf(
            Category("Transfer", Icon.I14, TransactionType.TRANSFER, "TRANSFER"),
        )

        fun localizeCategoriesNames(categories: List<Category>, context: Context) : List<Category> {
            for (category in categories) {
                if (categoriesNamesResourceMap.containsKey(category.name.lowercase())) {
                    category.name = context.getString(categoriesNamesResourceMap[category.name.lowercase()]!!)
                }
            }
            return categories
        }

        private val categoriesNamesResourceMap: Map<String, Int> = mapOf(
            Pair("food", R.string.food),
            Pair("clothes", R.string.clothes),
            Pair("health", R.string.health),
            Pair("house", R.string.house),
            Pair("transport", R.string.transport),
            Pair("travel", R.string.travel),
            Pair("subscriptions", R.string.subscriptions),
            Pair("entertainment", R.string.entertainment),
            Pair("sport", R.string.sport),
            Pair("beauty", R.string.beauty),
            Pair("presents", R.string.presents),
            Pair("education", R.string.education),
            Pair("equipment", R.string.equipment),
            Pair("other", R.string.other),
            Pair("wages", R.string.wages),
            Pair("cashback", R.string.cashback),
            Pair("transfer", R.string.transfer),
        )
    }
}

enum class CategoryValidationType{
    VALID, UNKNOWN_ICON_INVALID
}

class InvalidCategoryException(message: String, validationType: CategoryValidationType) : Exception(message) {
    constructor(validationType: CategoryValidationType): this(when(validationType) {
        CategoryValidationType.VALID -> "CRITICAL ERROR!!! Category is valid but InvalidTransactionException thrown."
        CategoryValidationType.UNKNOWN_ICON_INVALID -> "Invalid Icon Provided"
    }, validationType)
}