package com.colleagues.austrom.models

import com.colleagues.austrom.R

class Category(val name: String? = null,
               val type: String? = null,
               val imgReference: Int? = null,
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
            Category("Food", "Mandatory", R.drawable.ic_category_food_temp, TransactionType.EXPENSE),
            Category("Clothes", "Mandatory", R.drawable.ic_category_clothes_temp, TransactionType.EXPENSE),
            Category("Health", "Mandatory", R.drawable.ic_category_health_temp, TransactionType.EXPENSE),
            Category("House", "Mandatory", R.drawable.ic_category_rent_temp, TransactionType.EXPENSE),
            Category("Transport", "Mandatory", R.drawable.ic_category_transport_temp, TransactionType.EXPENSE),
            Category("Travel", "Optional", R.drawable.ic_category_travel_temp, TransactionType.EXPENSE),
            Category("Subscriptions", "Optional", R.drawable.ic_category_subscriptions_temp, TransactionType.EXPENSE),
            Category("Entertainment", "Optional", R.drawable.ic_category_entertainment_temp, TransactionType.EXPENSE),
            Category("Sport", "Optional", R.drawable.ic_category_sport_temp, TransactionType.EXPENSE),
            Category("Beauty", "Optional", R.drawable.ic_category_beauty_temp, TransactionType.EXPENSE),
            Category("Presents", "Optional", R.drawable.ic_category_presents_temp, TransactionType.EXPENSE),
            Category("Education", "Optional", R.drawable.ic_category_education_temp, TransactionType.EXPENSE),
            Category("Equipment", "Optional", R.drawable.ic_category_equipment_temp, TransactionType.EXPENSE),
            Category("Other", "Optional", R.drawable.ic_category_other_temp, TransactionType.EXPENSE),
        )

        var defaultIncomeCategories : List<Category> = listOf(
            Category("Wages", "Optional", R.drawable.ic_category_wages_temp, TransactionType.INCOME),
            Category("Cashback", "Optional", R.drawable.ic_category_cashback_temp, TransactionType.INCOME),
        )

        var defaultTransferCategories : List<Category> = listOf(
            Category("Transfer", "Optional", R.drawable.ic_category_transfer_temp, TransactionType.TRANSFER),
        )
    }
}