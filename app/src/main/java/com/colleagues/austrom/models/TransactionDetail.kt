package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(foreignKeys = [ForeignKey(entity = Transaction::class,
        parentColumns = ["transactionId"],
        childColumns = ["transactionId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["transactionId"])])
class TransactionDetail(val transactionId: String, val name: String, val cost: Double, val quantity: Double? = null, val typeOfQuantity: QuantityUnit? = null, val categoryName: String? = null) {
    @PrimaryKey(autoGenerate = false)
    var transactionDetailId: String = generateUniqueTransactionKey()

    companion object{
        fun generateUniqueTransactionKey() : String {
            return UUID.randomUUID().toString()
        }
    }
}

enum class QuantityUnit {
    KG, PC, L, M
}