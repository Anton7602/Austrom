package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CurrencyRecyclerAdapter
import com.colleagues.austrom.fragments.SettingsFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CurrencySelectionDialogFragment(private val currencyReceiver: SettingsFragment) : BottomSheetDialogFragment() {
    private lateinit var currencyHolder: RecyclerView
    private lateinit var declineButton: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_currency_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)

        currencyHolder.layoutManager = LinearLayoutManager(activity)
        currencyHolder.adapter = CurrencyRecyclerAdapter(AustromApplication.activeCurrencies, this, currencyReceiver)

        declineButton.setOnClickListener {
            this.dismiss()
        }
    }

    private fun bindViews(view: View) {
        currencyHolder = view.findViewById(R.id.csdial_currencyholder_rcv)
        declineButton = view.findViewById(R.id.csdial_decline_btn)
    }

}