package com.colleagues.austrom.dialogs.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CurrencyRecyclerAdapter
import com.colleagues.austrom.models.Currency
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CurrencySelectionDialogFragment(private val selectedCurrency: Currency?): BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_currency_selection, container, false) }
    fun setOnDialogResultListener(l: (Currency)->Unit) { returnResult = l }
    private var returnResult: (Currency)->Unit = {}
    //region Binding
    private lateinit var currencyHolder: RecyclerView
    private lateinit var declineButton: ImageButton
    private lateinit var searchField: EditText
    private lateinit var dialogHolder: CardView
    private lateinit var searchFieldHolder: CardView
    private fun bindViews(view: View) {
        currencyHolder = view.findViewById(R.id.csdial_currencyholder_rcv)
        declineButton = view.findViewById(R.id.csdial_decline_btn)
        searchField = view.findViewById(R.id.csdial_search_txt)
        dialogHolder = view.findViewById(R.id.csdial_holder_crv)
        searchFieldHolder = view.findViewById(R.id.csdial_searchHolder_crv)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setDialogStyle()
        setUpRecyclerView()
        declineButton.setOnClickListener { this.dismiss() }
        searchField.addTextChangedListener { filterCurrenciesList(searchField.text.toString()) }
    }

    private fun filterCurrenciesList(searchText: String) {
        val adapter = if (searchText.isNotEmpty()) {
            CurrencyRecyclerAdapter(AustromApplication.activeCurrencies.filter { entry -> entry.value.name.contains(searchText, ignoreCase = true)}, requireActivity() as AppCompatActivity,
                selectedCurrency, isSortingByMyCurrencies = false, isSortingByBaseCurrencies = false)
        } else {
            CurrencyRecyclerAdapter(AustromApplication.activeCurrencies, requireActivity() as AppCompatActivity, selectedCurrency)
        }
        adapter.setOnItemClickListener{currency -> returnResult(currency); dismiss() }
        currencyHolder.adapter = adapter
    }

    private fun setUpRecyclerView() {
        currencyHolder.layoutManager = LinearLayoutManager(activity)
        val adapter = CurrencyRecyclerAdapter(AustromApplication.activeCurrencies, requireActivity() as AppCompatActivity, selectedCurrency)
        adapter.setOnItemClickListener { currency -> returnResult(currency); dismiss() }
        currencyHolder.adapter = adapter
    }

    private fun setDialogStyle() {
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        searchFieldHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
    }
}