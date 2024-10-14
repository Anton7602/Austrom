package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.dialogs.LanguageSelectionDialogFragment
import com.colleagues.austrom.dialogs.QuickAccessPinDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.views.SettingsButtonView

class SettingsFragment : Fragment(R.layout.fragment_settings), IDialogInitiator {
    private lateinit var baseCurrencySetting: SettingsButtonView
    private lateinit var quickAccessPinSetting: SettingsButtonView
    private lateinit var languageSetting: SettingsButtonView


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
        languageSetting.setOnClickListener {
            LanguageSelectionDialogFragment().show(requireActivity().supportFragmentManager, "Language Selection Dialog")
        }
    }

    override fun receiveValue(value: String, valueType: String) {
        when (valueType) {
            "BaseCurrency" -> baseCurrencySetting.setValueText(value)
            "QuickAccessCode" -> quickAccessPinSetting.setValueText("****")
        }
    }

    private fun setSettingsValues() {
        baseCurrencySetting.setValueText(AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.name ?: "Unknown currency")
        quickAccessPinSetting.setValueText(if ((requireActivity().application as AustromApplication).getRememberedPin()!=null) {"****"} else {"Disabled"})
        languageSetting.setValueText(resources.configuration.locales.get(0).displayLanguage.startWithUppercase())
    }

    private fun bindViews(view: View) {
        baseCurrencySetting = view.findViewById(R.id.set_baseCurrency_btn)
        quickAccessPinSetting = view.findViewById(R.id.set_quickPin_btn)
        languageSetting = view.findViewById(R.id.set_appLocale_btn)
    }
}