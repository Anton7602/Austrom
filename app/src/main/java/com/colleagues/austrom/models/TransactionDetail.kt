package com.colleagues.austrom.models

class TransactionDetail(val name: String? = null,
                        val quantity: Double? = null,
                        val typeOfQuantity: QuantityUnit? = null,
                        val cost: Double? = null,
                        val categoryName: String? = null)

enum class QuantityUnit {
    KG, PC, L, M
}