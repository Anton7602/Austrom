package com.colleagues.austrom.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colleagues.austrom.AustromApplication.Companion.activeCurrencies
import com.colleagues.austrom.AustromApplication.Companion.appUser
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.google.firebase.database.Exclude
import java.util.UUID

@Entity
data class User private constructor(var username: String,
                                    val email: String,
                                    var password: String,
                                    var baseCurrencyCode: String = "USD",
                                    var activeBudgetId: String?=null,
                                    var tokenId: String? = null,
                                    var primaryPaymentMethod: String? = null,
                                    @PrimaryKey(autoGenerate = false) @Exclude
                                    var userId: String = generateUniqueUserId()) {

    private constructor(): this("", "", "", "", null, null, null, "")
    constructor(username: String, email: String, password: String, baseCurrencyCode: String = "USD"): this(username, email.lowercase(), password, baseCurrencyCode,null, null, generateUniqueUserId())

    companion object {
        fun generateUniqueUserId() : String {
            return UUID.randomUUID().toString()
        }
    }

    public fun setNewBaseCurrency(currency: Currency, localDBProvider: LocalDatabaseProvider, remoteDbProvider: IRemoteDatabaseProvider? = null) {
        this.baseCurrencyCode = currency.code
        localDBProvider.updateUser(this)
        remoteDbProvider?.updateUser(this)
        Currency.switchRatesToNewBaseCurrency(activeCurrencies, currency.code)
    }
}
