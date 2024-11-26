package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.CategoryControlDialogFragment
import com.colleagues.austrom.dialogs.CategoryCreationDialogFragment
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.dialogs.LanguageSelectionDialogFragment
import com.colleagues.austrom.dialogs.QuickAccessPinDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.views.SettingsButtonView

class SettingsFragment : Fragment(R.layout.fragment_settings), IDialogInitiator {
    private lateinit var baseCurrencySetting: SettingsButtonView
    private lateinit var categorySetting: SettingsButtonView
    private lateinit var quickAccessPinSetting: SettingsButtonView
    private lateinit var languageSetting: SettingsButtonView
    private lateinit var moneyFormatSetting: SettingsButtonView

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
        categorySetting.setOnClickListener {
            CategoryControlDialogFragment(this).show(requireActivity().supportFragmentManager, "Category Control Dialog")
        }
    }

    override fun receiveValue(value: String, valueType: String) {
        when (valueType) {
            "BaseCurrency" -> {
                baseCurrencySetting.setValueText(value)
                moneyFormatSetting.setValueText("${1234567.89.toMoneyFormat()} ${AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol}")
            }
            "QuickAccessCode" -> quickAccessPinSetting.setValueText("****")
        }
    }

    private fun setSettingsValues() {
        baseCurrencySetting.setValueText(AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.name ?: "Unknown currency")
        quickAccessPinSetting.setValueText(if ((requireActivity().application as AustromApplication).getRememberedPin()!=null) {"****"} else {"Disabled"})
        languageSetting.setValueText(resources.configuration.locales.get(0).displayLanguage.startWithUppercase())
        moneyFormatSetting.setValueText("${1234567.89.toMoneyFormat()} ${AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol}")
        val dbProvider = LocalDatabaseProvider(requireActivity())
        var categoryLine = ""
        dbProvider.getCategories().forEach{ category ->
            categoryLine += "$category, "
        }
        if (categoryLine.isNotEmpty()) {
            categoryLine = categoryLine.dropLast(2)
            categorySetting.setValueText(categoryLine)
        }
    }

    private fun bindViews(view: View) {
        baseCurrencySetting = view.findViewById(R.id.set_baseCurrency_btn)
        quickAccessPinSetting = view.findViewById(R.id.set_quickPin_btn)
        languageSetting = view.findViewById(R.id.set_appLocale_btn)
        categorySetting = view.findViewById(R.id.set_categories_btn)
        moneyFormatSetting = view.findViewById(R.id.set_moneyFormat_btn)
    }

    override fun onResume() {
        super.onResume()
        setSettingsValues()
    }
}