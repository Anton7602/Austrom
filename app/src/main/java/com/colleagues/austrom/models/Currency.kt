package com.colleagues.austrom.models

class Currency(val currencyId: String, val fullName : String, val symbol : String) {

    companion object{
        val defaultCurrencies = listOf<Currency>(
            Currency("USD", "Dollar", "$"),
            Currency("EUR", "Euro", "€"),
            Currency("RUB", "Ruble", "₽")
        )
    }

}