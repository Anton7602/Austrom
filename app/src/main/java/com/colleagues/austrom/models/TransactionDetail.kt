package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.parseToDouble
import java.util.UUID

@Entity(foreignKeys = [ForeignKey(entity = Transaction::class,
        parentColumns = ["transactionId"],
        childColumns = ["transactionId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["transactionId"])])
class TransactionDetail(val transactionId: String, val name: String, val cost: Double, val quantity: Double? = null, val typeOfQuantity: QuantityUnit? = null, val categoryName: String? = null,
    @PrimaryKey(autoGenerate = false)
    var transactionDetailId: String = generateUniqueTransactionKey()) {

    fun serialize(): String {return "$transactionDetailId,$transactionId,$name,$cost,$quantity,$typeOfQuantity,$categoryName" }

    companion object{
        fun generateUniqueTransactionKey() : String {
            return UUID.randomUUID().toString()
        }

        fun deserialize(serializedDetail: String): TransactionDetail {
            val dataParts = serializedDetail.split(",")
            return TransactionDetail(
                transactionDetailId = dataParts[0],
                transactionId = dataParts[1],
                name = dataParts[2],
                cost = dataParts[3].parseToDouble() ?: 0.0,
                quantity = dataParts[4].parseToDouble() ?: 0.0,
                typeOfQuantity = when(dataParts[5]) {
                    "KG" -> QuantityUnit.KG
                    "PC" -> QuantityUnit.PC
                    "L" -> QuantityUnit.L
                    "M" -> QuantityUnit.M
                    else -> null
                },
                categoryName = dataParts[6]
            )
        }
    }
}

enum class QuantityUnit(val fullNameResourceId: Int, val shortNameResourceId: Int) {
    KG(R.string.kilogram, R.string.kg), PC(R.string.piece, R.string.pc), L(R.string.litre, R.string.l), M(R.string.meter, R.string.m)
}