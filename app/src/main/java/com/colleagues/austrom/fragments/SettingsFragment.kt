package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.dialogs.QuickAccessPinDialogFragment
import com.colleagues.austrom.interfaces.IDialogInitiator

class SettingsFragment : Fragment(R.layout.fragment_settings), IDialogInitiator {
    private lateinit var baseCurrencySetting: CardView
    private lateinit var baseCurrencyValue: TextView
    private lateinit var quickAccessPinSetting: CardView
    private lateinit var quickAccessPinValue: TextView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setSettingsValues()
        baseCurrencySetting.setOnClickListener {
            CurrencySelectionDialogFragment(this).show(requireActivity().supportFragmentManager, "Currency Selection Dialog")
        }
        quickAccessPinSetting.setOnClickListener {
            QuickAccessPinDialogFragment(this).show(requireActivity().supportFragmentManager, "Pin Changing Dialog")
        }
    }

    override fun receiveValue(value: String, valueType: String) {
        when (valueType) {
            "BaseCurrency" -> baseCurrencyValue.text = value
            "QuickAccessCode" -> quickAccessPinValue.text = "****"
        }
    }

    private fun setSettingsValues() {
        baseCurrencyValue.text = AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.name
        quickAccessPinValue.text =
            if ((requireActivity().application as AustromApplication).getRememberedPin()!=null) {"****"} else {"Disabled"}
    }

    private fun bindViews(view: View) {
        baseCurrencySetting = view.findViewById(R.id.set_baseCurrency_cdv)
        baseCurrencyValue = view.findViewById(R.id.set_baseCurrencyName_txt)
        quickAccessPinSetting = view.findViewById(R.id.set_quickPin_cdv)
        quickAccessPinValue = view.findViewById(R.id.set_quickPinValue_txt)
    }
}