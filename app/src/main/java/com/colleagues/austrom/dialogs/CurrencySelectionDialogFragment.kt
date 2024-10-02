package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CurrencyRecyclerAdapter
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CurrencySelectionDialogFragment(private val receiver: IDialogInitiator?) : BottomSheetDialogFragment(), IDialogInitiator {
    private lateinit var currencyHolder: RecyclerView
    private lateinit var declineButton: ImageButton
    private lateinit var searchField: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_currency_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        currencyHolder.layoutManager = LinearLayoutManager(activity)
        currencyHolder.adapter = CurrencyRecyclerAdapter(AustromApplication.activeCurrencies, this, receiver)

        declineButton.setOnClickListener {
            this.dismiss()
        }

        searchField.addTextChangedListener {
            currencyHolder.adapter = if (searchField.text.isNotEmpty()) {
                CurrencyRecyclerAdapter(AustromApplication.activeCurrencies.filter { entry -> entry.value.name.contains(searchField.text, ignoreCase = true)},
                    this, receiver)
            } else {
                CurrencyRecyclerAdapter(AustromApplication.activeCurrencies, this, receiver)
            }
        }
    }

    override fun receiveValue(value: String, valueType: String) {
        (requireActivity().application as AustromApplication)
            .setNewBaseCurrency(AustromApplication.activeCurrencies[value]!!)
        dismiss()
    }

    private fun bindViews(view: View) {
        currencyHolder = view.findViewById(R.id.csdial_currencyholder_rcv)
        declineButton = view.findViewById(R.id.csdial_decline_btn)
        searchField = view.findViewById(R.id.csdial_search_txt)
    }
}