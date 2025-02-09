package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.views.PeriodType
import java.time.LocalDate
import java.util.UUID

@Entity
class Plan(val planDate: LocalDate, var planType: PeriodType, var planValue: Double,
    var planName: String,@PrimaryKey(autoGenerate = false) val planId: String = generateUniquePlanKey()) {

    companion object {
        fun generateUniquePlanKey(): String {
            return UUID.randomUUID().toString()
        }
    }
}