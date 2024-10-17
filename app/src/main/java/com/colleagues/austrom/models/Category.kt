package com.colleagues.austrom.models

import android.content.Context
import com.colleagues.austrom.R
import com.colleagues.austrom.managers.Icon

class Category(var name: String? = null,
               val type: String? = null,
               val imgReference: Icon? = null,
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
        var defaultExpenseCategories : List<Category> = listOf(
            Category("Food", "Mandatory", Icon.I7, TransactionType.EXPENSE),
            Category("Clothes", "Mandatory", Icon.I3, TransactionType.EXPENSE),
            Category("Health", "Mandatory", Icon.I8, TransactionType.EXPENSE),
            Category("House", "Mandatory", Icon.I11, TransactionType.EXPENSE),
            Category("Transport", "Mandatory", Icon.I15, TransactionType.EXPENSE),
            Category("Travel", "Optional", Icon.I16, TransactionType.EXPENSE),
            Category("Subscriptions", "Optional", Icon.I13, TransactionType.EXPENSE),
            Category("Entertainment", "Optional", Icon.I5, TransactionType.EXPENSE),
            Category("Sport", "Optional", Icon.I12, TransactionType.EXPENSE),
            Category("Beauty", "Optional", Icon.I1, TransactionType.EXPENSE),
            Category("Presents", "Optional", Icon.I10, TransactionType.EXPENSE),
            Category("Education", "Optional", Icon.I4, TransactionType.EXPENSE),
            Category("Equipment", "Optional", Icon.I6, TransactionType.EXPENSE),
            Category("Other", "Optional", Icon.I9, TransactionType.EXPENSE),
        )

        var defaultIncomeCategories : List<Category> = listOf(
            Category("Wages", "Optional", Icon.I62, TransactionType.INCOME),
            Category("Cashback", "Optional", Icon.I2, TransactionType.INCOME),
        )

        var defaultTransferCategories : List<Category> = listOf(
            Category("Transfer", "Optional", Icon.I14, TransactionType.TRANSFER),
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