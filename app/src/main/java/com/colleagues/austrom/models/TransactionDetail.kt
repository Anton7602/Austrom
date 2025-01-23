package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.colleagues.austrom.R
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

enum class QuantityUnit(val fullNameResourceId: Int, val shortNameResourceId: Int) {
    KG(R.string.kilogramm, R.string.kg), PC(R.string.piece, R.string.pc), L(R.string.litre, R.string.l), M(R.string.meter, R.string.m)
}