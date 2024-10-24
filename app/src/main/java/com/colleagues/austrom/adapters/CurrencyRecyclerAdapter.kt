package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Currency

class CurrencyRecyclerAdapter(private var currencies: Map<String, Currency>,
                              private var currencySelector: IDialogInitiator,
                              private var currencyReceiver: IDialogInitiator?,
                              private var activity: AppCompatActivity,
                              private val isSortingByBaseCurrencies: Boolean = true): RecyclerView.Adapter<CurrencyRecyclerAdapter.CurrencyViewHolder>() {
    class CurrencyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val currencyHolder: CardView = itemView.findViewById(R.id.curit_currencyHolder_crv)
        val currencySymbol: TextView = itemView.findViewById(R.id.curit_currencySymbol_txt)
        val currencyCode: TextView = itemView.findViewById(R.id.curit_currencyCode_txt)
        val currencyName: TextView = itemView.findViewById(R.id.curit_currencyName_txt)
        val selectionMarker: RadioButton = itemView.findViewById(R.id.curit_selectionMarker_rbn)
        val currencyExchangeRate: TextView = itemView.findViewById(R.id.curit_currencyExchangeRate_txt)
        val currencyGroupSeparator: CardView = itemView.findViewById(R.id.curit_categoryDivider_crd)
        val currencyGroupHeader: TextView = itemView.findViewById(R.id.curit_categoryHeader_txt)
    }

    private val baseCurrencyCodes: List<String> = listOf("USD", "EUR","GBP", "JPY", "CNY", "CHF", "AUD", "CAD")
    private var baseCurrencyCategoryEndIndex = 0
    init {
        if (isSortingByBaseCurrencies) {
            val resortedCurrencies: MutableMap<String, Currency> = mutableMapOf()
            for (currencyCode in baseCurrencyCodes) {
                if (currencies[currencyCode]!=null) {
                    resortedCurrencies[currencyCode] = currencies[currencyCode]!!
                    baseCurrencyCategoryEndIndex++
                }
            }
            for (currency in currencies) {
                if (!resortedCurrencies.containsKey(currency.key)) {
                    resortedCurrencies[currency.key] = currency.value
                }
            }
            currencies = resortedCurrencies
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        return CurrencyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_currency, parent, false))
    }

    override fun getItemCount(): Int {
        return currencies.size
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currency = currencies.values.elementAt(position)
        if (isSortingByBaseCurrencies) {
            holder.currencyGroupSeparator.visibility = if(position == 0 || position == baseCurrencyCategoryEndIndex) View.VISIBLE else View.GONE
            holder.currencyGroupHeader.text = if (position==0 && baseCurrencyCategoryEndIndex!=0) activity.getString( R.string.popular_currencies)
            else activity.getString(R.string.all_currencies)
        }
        val baseCurrencyCode = AustromApplication.appUser?.baseCurrencyCode
        holder.currencyName.text = currency.name
        holder.currencyCode.text = currency.code
        holder.selectionMarker.isChecked = (currency.code == baseCurrencyCode)
        holder.currencySymbol.text = baseCurrencyCode
        holder.currencyExchangeRate.text = (1/currency.exchangeRate).toMoneyFormat()

        val currencyTapOnClickListener = View.OnClickListener { _ ->
            currencySelector.receiveValue(currency.code, "CurrencyCode")
            currencyReceiver?.receiveValue(currency.name, "BaseCurrency")
            holder.selectionMarker.isChecked = true
        }
        holder.currencyHolder.setOnClickListener (currencyTapOnClickListener)
        holder.selectionMarker.setOnClickListener (currencyTapOnClickListener)
    }
}