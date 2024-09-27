package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.fragments.SettingsFragment
import com.colleagues.austrom.models.Currency

class CurrencyRecyclerAdapter(private var currencies: Map<String, Currency>,
                              private var currencySelector: CurrencySelectionDialogFragment,
                              private var currencyReceiver: SettingsFragment): RecyclerView.Adapter<CurrencyRecyclerAdapter.CurrencyViewHolder>() {
    class CurrencyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val currencyHolder: CardView = itemView.findViewById(R.id.curit_currencyHolder_crv)
        val currencySymbol: TextView = itemView.findViewById(R.id.curit_currencySymbol_txt)
        val currencyCode: TextView = itemView.findViewById(R.id.curit_currencyCode_txt)
        val currencyName: TextView = itemView.findViewById(R.id.curit_currencyName_txt)
        val selectionMarker: RadioButton = itemView.findViewById(R.id.curit_selectionMarker_rbn)
        val currencyExchangeRate: TextView = itemView.findViewById(R.id.curit_currencyExchangeRate_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        return CurrencyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_currency, parent, false))
    }

    override fun getItemCount(): Int {
        return currencies.size
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currency = currencies.values.elementAt(position)
        val baseCurrencyCode = AustromApplication.appUser?.baseCurrencyCode
        holder.currencyName.text = currency.name
        holder.currencyCode.text = currency.code
        if (currency.code == baseCurrencyCode) {
            holder.selectionMarker.isChecked = true
        }
        holder.currencySymbol.text = baseCurrencyCode
        holder.currencyExchangeRate.text = String.format("%.3f", 1/currency.exchangeRate)
        holder.currencyHolder.setOnClickListener {
            currencyReceiver.setNewBaseCurrency(currency)
            holder.selectionMarker.isChecked = true
            currencySelector.dismiss()
        }
    }
}