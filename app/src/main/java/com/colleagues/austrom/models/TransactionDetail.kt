package com.colleagues.austrom.models

class TransactionDetail(val name: String,
                        val quantity: Double?,
                        val typeOfQuantity: QuantityUnit?,
                        val cost: Double,
                        val category: Category) {
}

enum class QuantityUnit {
    KG, PC, L, M
}