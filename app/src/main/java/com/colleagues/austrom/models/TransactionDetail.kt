package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(foreignKeys = [ForeignKey(entity = Transaction::class,
        parentColumns = ["transactionId"],
        childColumns = ["transactionId"],
        onDelete = ForeignKey.NO_ACTION)])
class TransactionDetail(
    @PrimaryKey
    var transactionDetailId: String = "",
    val transactionId: String = "",
    val name: String? = null,
    val quantity: Double? = null,
    val typeOfQuantity: QuantityUnit? = null,
    val cost: Double? = null,
    val categoryName: String? = null) {

    companion object{
        fun generateUniqueTransactionKey() : String {
            return UUID.randomUUID().toString()
        }
    }
}

enum class QuantityUnit {
    KG, PC, L, M
}