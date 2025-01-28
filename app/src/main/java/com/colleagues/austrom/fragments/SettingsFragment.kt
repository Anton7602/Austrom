package com.colleagues.austrom.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.dialogs.CategoryControlDialogFragment
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.dialogs.LanguageSelectionDialogFragment
import com.colleagues.austrom.dialogs.QuickAccessPinDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.SettingsButtonView

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    //region Binding
    private lateinit var baseCurrencySetting: SettingsButtonView
    private lateinit var categorySetting: SettingsButtonView
    private lateinit var quickAccessPinSetting: SettingsButtonView
    private lateinit var languageSetting: SettingsButtonView
    private lateinit var moneyFormatSetting: SettingsButtonView

    private fun bindViews(view: View) {
        baseCurrencySetting = view.findViewById(R.id.set_baseCurrency_btn)
        quickAccessPinSetting = view.findViewById(R.id.set_quickPin_btn)
        languageSetting = view.findViewById(R.id.set_appLocale_btn)
        categorySetting = view.findViewById(R.id.set_categories_btn)
        moneyFormatSetting = view.findViewById(R.id.set_moneyFormat_btn)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setSettingsValues()
        baseCurrencySetting.setOnClickListener { launchCurrencyDialog() }
        quickAccessPinSetting.setOnClickListener { launchQuickAccessPinDialog() }
        languageSetting.setOnClickListener { launchLanguageSelectionDialog() }
        categorySetting.setOnClickListener { launchCategoryControlDialog() }
    }

    private fun launchCurrencyDialog() {
        val dialog = CurrencySelectionDialogFragment(AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode])
        dialog.setOnDialogResultListener { currency ->
            (requireActivity().application as AustromApplication).setNewBaseCurrency(currency)
            baseCurrencySetting.setValueText(currency.name)
            moneyFormatSetting.setValueText("${1234567.89.toMoneyFormat()} ${AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol}")
        }
        dialog.show(requireActivity().supportFragmentManager, "Currency Selection Dialog")
    }

    private fun launchQuickAccessPinDialog() {
        val dialog = QuickAccessPinDialogFragment()
        dialog.setOnDialogResultListener { _ -> quickAccessPinSetting.setValueText("****") }
        dialog.show(requireActivity().supportFragmentManager, "Pin Changing Dialog")
    }

    private fun launchLanguageSelectionDialog() {
        val dialog = LanguageSelectionDialogFragment()
        dialog.setOnDialogResultListener { locale ->
            (requireActivity().application as AustromApplication).setApplicationLanguage(locale.language)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale.language))
            } else  {
                requireActivity().recreate()
            }}
        dialog.show(requireActivity().supportFragmentManager, "Language Selection Dialog")
    }

    private fun launchCategoryControlDialog() {
        val dialog = CategoryControlDialogFragment()
        dialog.setOnDialogResultListener { category ->
            AustromApplication.activeCategories[category.categoryId] = category
            categorySetting.setValueText(updateCategoriesListLine())
        }
        dialog.show(requireActivity().supportFragmentManager, "Category Control Dialog")
    }


    private fun setSettingsValues() {
        baseCurrencySetting.setValueText(AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.name ?: "Unknown currency")
        quickAccessPinSetting.setValueText(if ((requireActivity().application as AustromApplication).getRememberedPin()!=null) {"****"} else {"Disabled"})
        languageSetting.setValueText(resources.configuration.locales.get(0).displayLanguage.startWithUppercase())
        moneyFormatSetting.setValueText("${1234567.89.toMoneyFormat()} ${AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol}")
        categorySetting.setValueText(updateCategoriesListLine())

    }

    private fun updateCategoriesListLine(): String {
        var categoryLine = ""
        AustromApplication.activeCategories.values.filter { l -> l.transactionType!=TransactionType.TRANSFER }.forEach{ category -> categoryLine += "${category.name}, "  }
        if (categoryLine.isNotEmpty()) {
            categoryLine = categoryLine.dropLast(2)
        }
        return  categoryLine
    }

    override fun onResume() {
        super.onResume()
        setSettingsValues()
    }
}