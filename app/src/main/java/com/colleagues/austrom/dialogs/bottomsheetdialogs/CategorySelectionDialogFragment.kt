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
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CategorySelectionDialogFragment(private val transactionType: TransactionType) : BottomSheetDialogFragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_category_selection, container, false) }
    fun setOnDialogResultListener(l: (Category)->Unit) { returnResult = l }
    private var returnResult: (Category)->Unit = {}
    //region Binding
    private lateinit var categoryHolder: RecyclerView
    private lateinit var declineButton: ImageButton
    private lateinit var searchField: EditText
    private lateinit var dialogHolder: CardView
    private lateinit var searchFieldHolder: CardView
    private fun bindViews(view: View) {
        categoryHolder = view.findViewById(R.id.catseldial_currencyholder_rcv)
        declineButton = view.findViewById(R.id.catseldial_decline_btn)
        searchField = view.findViewById(R.id.catseldial_search_txt)
        dialogHolder = view.findViewById(R.id.catseldial_holder_crv)
        searchFieldHolder = view.findViewById(R.id.catseldial_searchHolder_crv)
    }
    ///endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        searchFieldHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        declineButton.setOnClickListener { this.dismiss() }
        setUpRecyclerViews()
        searchField.addTextChangedListener { filterCurrenciesList(searchField.text.toString()) }
    }

    private fun filterCurrenciesList(searchText: String) {
        val adapter = if (searchText.isNotEmpty()) {
            CategoryRecyclerAdapter(AustromApplication.activeCategories.values.filter { l -> l.name.lowercase().contains(searchText.lowercase()) && l.transactionType==transactionType }.toMutableList(), requireActivity() as AppCompatActivity,  false, false)
        } else {
            CategoryRecyclerAdapter((AustromApplication.activeCategories.values.filter { l -> l.transactionType==transactionType }).toMutableList(), requireActivity() as AppCompatActivity,  false, false)
        }
        adapter.setOnItemClickListener{currency -> returnResult(currency); dismiss() }
        categoryHolder.adapter = adapter
    }

    private fun setUpRecyclerViews() {
        categoryHolder.layoutManager = LinearLayoutManager(activity)
        val adapter = CategoryRecyclerAdapter(AustromApplication.activeCategories.values.filter { l -> l.transactionType == transactionType }.toMutableList(), requireActivity() as AppCompatActivity,  false, false)
        adapter.setOnItemClickListener { category -> returnResult(category); dismiss() }
        categoryHolder.adapter = adapter
    }
}