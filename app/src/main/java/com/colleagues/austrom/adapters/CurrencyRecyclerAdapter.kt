package com.colleagues.austrom.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Currency

class CurrencyRecyclerAdapter(private var currencies: Map<String, Currency>, private val context: Context, private var selectedCurrency: Currency? = null, private val isSortingByMyCurrencies: Boolean = true, private val isSortingByBaseCurrencies: Boolean = true): RecyclerView.Adapter<CurrencyRecyclerAdapter.CurrencyViewHolder>() {
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
    private var returnClickedItem: (Currency)->Unit = {}
    fun setOnItemClickListener(l: ((Currency)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder { return CurrencyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_currency, parent, false)) }
    override fun getItemCount(): Int { return currencies.size }

    private val baseCurrencyCodes: MutableList<String> = mutableListOf("USD", "EUR","GBP", "JPY", "CNY", "CHF", "AUD", "CAD")
    private var baseCurrencyCategoryEndIndex = 0
    private var myCurrencyCategoryEndIndex = 0


    init {
        if (isSortingByMyCurrencies || isSortingByBaseCurrencies) {
            val resortedCurrencies: MutableMap<String, Currency> = mutableMapOf()
            if (isSortingByBaseCurrencies) {
                if (currencies[AustromApplication.appUser?.baseCurrencyCode]!=null) {
                    resortedCurrencies[AustromApplication.appUser!!.baseCurrencyCode] = currencies[AustromApplication.appUser?.baseCurrencyCode]!!
                    baseCurrencyCategoryEndIndex++
                    myCurrencyCategoryEndIndex++
                }
                AustromApplication.activeAssets.values.forEach { asset ->
                    if (currencies[asset.currencyCode]!=null && !resortedCurrencies.containsKey(asset.currencyCode)) {
                        resortedCurrencies[asset.currencyCode] = currencies[asset.currencyCode]!!
                        baseCurrencyCategoryEndIndex++
                        myCurrencyCategoryEndIndex++
                    }
                }
            }
            if (isSortingByBaseCurrencies) {
                for (currencyCode in baseCurrencyCodes) {
                    if (currencies[currencyCode]!=null && !resortedCurrencies.containsKey(currencyCode)) {
                        resortedCurrencies[currencyCode] = currencies[currencyCode]!!
                        baseCurrencyCategoryEndIndex++
                    }
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

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currency = currencies.values.elementAt(position)
        if (isSortingByBaseCurrencies || isSortingByMyCurrencies) {
            holder.currencyGroupSeparator.visibility = if(position == 0 || position == baseCurrencyCategoryEndIndex || position==myCurrencyCategoryEndIndex) View.VISIBLE else View.GONE
            holder.currencyGroupHeader.text = if (position==0 && myCurrencyCategoryEndIndex!=0) context.getString( R.string.my_currencies)
            else if (position==0 && baseCurrencyCategoryEndIndex!=0) context.getString(R.string.popular_currencies)
            else if (position==myCurrencyCategoryEndIndex && baseCurrencyCategoryEndIndex>myCurrencyCategoryEndIndex) context.getString(R.string.popular_currencies)
            else context.getString(R.string.all_currencies)
        }
        val baseCurrencyCode = AustromApplication.appUser?.baseCurrencyCode
        holder.currencyName.text = currency.name
        holder.currencyCode.text = currency.code
        holder.selectionMarker.isChecked = (currency.code == selectedCurrency?.code)
        holder.currencySymbol.text = baseCurrencyCode
        holder.currencyExchangeRate.text = (1/currency.exchangeRate).toMoneyFormat()

        val currencyTapOnClickListener = View.OnClickListener { _ ->
            selectedCurrency = currency
            returnClickedItem(currency)
            holder.selectionMarker.isChecked = true
        }
        holder.currencyHolder.setOnClickListener (currencyTapOnClickListener)
        holder.selectionMarker.setOnClickListener (currencyTapOnClickListener)
    }
}