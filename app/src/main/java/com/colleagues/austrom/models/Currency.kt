package com.colleagues.austrom.models

class Currency(val code: String = "",
               val name : String = "",
               val symbol : String ="",
               var exchangeRate : Double = 0.0) {

    companion object{
        fun switchRatesToNewBaseCurrency(currenciesList: MutableMap<String, Currency>, newBaseCurrencyCode: String?) : MutableMap<String, Currency> {
            val newBaseCurrency = currenciesList[newBaseCurrencyCode]
            if (newBaseCurrency!=null) {
                val denominator = newBaseCurrency.exchangeRate
                for (currency in currenciesList) {
                    currency.value.exchangeRate /= denominator
                }
            }
            return currenciesList
        }
    }
}