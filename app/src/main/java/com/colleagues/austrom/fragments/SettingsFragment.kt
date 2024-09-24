package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.dialogs.TransactionCreationDialogFragment
import com.colleagues.austrom.models.Currency
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var baseCurrencySetting: CardView
    private lateinit var baseCurrencyValue: TextView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        baseCurrencyValue.text = AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.name

        baseCurrencySetting.setOnClickListener {
            CurrencySelectionDialogFragment(this).show(requireActivity().supportFragmentManager, "Currency Selection Dialog")
        }
    }

    fun setNewBaseCurrency(currency: Currency) {
        if (AustromApplication.appUser!=null) {
            val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
            AustromApplication.appUser!!.baseCurrencyCode = currency.code
            dbProvider.updateUser(AustromApplication.appUser!!)
            Currency.switchRatesToNewBaseCurrency(AustromApplication.activeCurrencies, currency.code)
            baseCurrencyValue.text = currency.name
        }
    }

    private fun bindViews(view: View) {
        baseCurrencySetting = view.findViewById(R.id.set_baseCurrency_cdv)
        baseCurrencyValue = view.findViewById(R.id.set_baseCurrencyName_txt)
    }
}