package com.colleagues.austrom.models

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.R
import com.colleagues.austrom.managers.Icon
import java.util.UUID

@Entity
class Category(
    @PrimaryKey(autoGenerate = false)
    var id: String = "",
    var name: String? = null,
    val type: String? = null,
    var imgReference: Icon? = null,
    val transactionType: TransactionType = TransactionType.EXPENSE) {

    override fun toString(): String {
        return this.name ?: ""
    }

    override fun equals(other: Any?): Boolean {
        val category = other as Category
        return (category.name==name && category.type==type && category.transactionType == transactionType)
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + transactionType.hashCode()
        return result
    }

    companion object{
        fun generateCategoryId(): String {
            return UUID.randomUUID().toString()
        }

        var defaultExpenseCategories : List<Category> = listOf(
            Category("Food", "Food", "Mandatory", Icon.I7, TransactionType.EXPENSE),
            Category("Clothes","Clothes", "Mandatory", Icon.I3, TransactionType.EXPENSE),
            Category("Health","Health", "Mandatory", Icon.I8, TransactionType.EXPENSE),
            Category("House","House", "Mandatory", Icon.I11, TransactionType.EXPENSE),
            Category("Transport","Transport", "Mandatory", Icon.I15, TransactionType.EXPENSE),
            Category("Travel","Travel", "Optional", Icon.I16, TransactionType.EXPENSE),
            Category("Subscriptions","Subscriptions", "Optional", Icon.I13, TransactionType.EXPENSE),
            Category("Entertainment","Entertainment", "Optional", Icon.I5, TransactionType.EXPENSE),
            Category("Sport","Sport", "Optional", Icon.I12, TransactionType.EXPENSE),
            Category("Beauty","Beauty", "Optional", Icon.I1, TransactionType.EXPENSE),
            Category("Presents","Presents", "Optional", Icon.I10, TransactionType.EXPENSE),
            Category("Education","Education", "Optional", Icon.I4, TransactionType.EXPENSE),
            Category("Equipment","Equipment", "Optional", Icon.I6, TransactionType.EXPENSE),
            Category("Other","Other", "Optional", Icon.I9, TransactionType.EXPENSE),
        )

        var defaultIncomeCategories : List<Category> = listOf(
            Category("Wages","Wages", "Optional", Icon.I62, TransactionType.INCOME),
            Category("Cashback","Cashback", "Optional", Icon.I2, TransactionType.INCOME),
        )

        var defaultTransferCategories : List<Category> = listOf(
            Category("Transfer","Transfer", "Optional", Icon.I14, TransactionType.TRANSFER),
        )

        fun localizeCategoriesNames(categories: List<Category>, context: Context) : List<Category> {
            for (category in categories) {
                if (categoriesNamesResourceMap.containsKey(category.name?.lowercase())) {
                    category.name = context.getString(categoriesNamesResourceMap[category.name!!.lowercase()]!!)
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