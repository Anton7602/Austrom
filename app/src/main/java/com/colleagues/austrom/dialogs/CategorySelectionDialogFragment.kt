package com.colleagues.austrom.dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
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
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter
import com.colleagues.austrom.adapters.CurrencyRecyclerAdapter
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CategorySelectionDialogFragment(private val transactionType: TransactionType, private val receiver: IDialogInitiator?) : BottomSheetDialogFragment(), IDialogInitiator {
    private lateinit var categoryHolder: RecyclerView
    private lateinit var declineButton: ImageButton
    private lateinit var searchField: EditText
    private lateinit var dialogHolder: CardView
    private lateinit var searchFieldHolder: CardView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_category_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        searchFieldHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)

        categoryHolder.layoutManager = LinearLayoutManager(activity)
        categoryHolder.adapter = when(transactionType) {
            TransactionType.EXPENSE -> CategoryRecyclerAdapter(AustromApplication.activeExpenseCategories.values.toMutableList(), requireActivity() as AppCompatActivity,  false, false, this)
            TransactionType.INCOME -> CategoryRecyclerAdapter(AustromApplication.activeIncomeCategories.values.toMutableList(), requireActivity() as AppCompatActivity,  false, false, this)
            TransactionType.TRANSFER -> CategoryRecyclerAdapter(AustromApplication.activeTransferCategories.values.toMutableList(), requireActivity() as AppCompatActivity,  false, false,this)
        }

        declineButton.setOnClickListener {
            this.dismiss()
        }

//        searchField.addTextChangedListener {
//            categoryHolder.adapter = if (searchField.text.isNotEmpty()) {
//                CategoryRecyclerAdapter(
//                    AustromApplication.activeCurrencies.filter { entry -> entry.value.name.contains(searchField.text, ignoreCase = true)},
//                    this, receiver, requireActivity() as AppCompatActivity, false, isSortingByBaseCurrencies = false)
//            } else {
//                CurrencyRecyclerAdapter(AustromApplication.activeCurrencies, this, receiver, requireActivity() as AppCompatActivity, true, true)
//            }
//        }
    }


    override fun receiveValue(value: String, valueType: String) {
        receiver?.receiveValue(value, valueType)
        dismiss()
    }

    private fun bindViews(view: View) {
        categoryHolder = view.findViewById(R.id.catseldial_currencyholder_rcv)
        declineButton = view.findViewById(R.id.catseldial_decline_btn)
        searchField = view.findViewById(R.id.catseldial_search_txt)
        dialogHolder = view.findViewById(R.id.catseldial_holder_crv)
        searchFieldHolder = view.findViewById(R.id.catseldial_searchHolder_crv)
    }
}