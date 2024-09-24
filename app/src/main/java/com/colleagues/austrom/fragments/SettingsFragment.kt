package com.colleagues.austrom.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.AssetCreationDialogFragment
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.models.Currency
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var currencyChips: ChipGroup
    private lateinit var addNewCurrency: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        addNewCurrency.setOnClickListener {
            CurrencySelectionDialogFragment(this).show(requireActivity().supportFragmentManager, "Currency Selection Dialog")
        }
    }

    fun addNewCurrency(currency: Currency) {
        val chip = Chip(requireActivity())
        chip.text = currency.code
        chip.setEnsureMinTouchTargetSize(false)
        chip.isCheckable = true
        currencyChips.addView(chip)
        chip.setOnClickListener {
            if (AustromApplication.appUser!=null) {
                val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(requireActivity())
                AustromApplication.appUser!!.baseCurrencyCode = currency.code
                dbProvider.updateUser(AustromApplication.appUser!!)
                Currency.switchRatesToNewBaseCurrency(AustromApplication.activeCurrencies, currency.code)
            }
        }
    }

    private fun bindViews(view: View) {
        addNewCurrency = view.findViewById(R.id.set_addCurrencies_btn)
        currencyChips = view.findViewById(R.id.set_selectedCurrecies_chgr)
    }
}