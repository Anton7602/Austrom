package com.colleagues.austrom.models

import com.colleagues.austrom.R
import com.colleagues.austrom.managers.Icon

class Category(val name: String? = null,
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
    }
}