package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryRecyclerAdapter
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CategoryControlDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_category_control, container, false) }
    fun setOnDialogResultListener(l: ((Category)->Unit)) { returnResult = l }
    private var returnResult: (Category)->Unit = {}
    //region Binding
    private lateinit var incomeCategoryHolder: RecyclerView
    private lateinit var expenseCategoryHolder: RecyclerView
    private lateinit var dialogHolder: CardView
    private lateinit var incomeSwitchButton: Button
    private lateinit var expenseSwitchButton: Button
    private fun bindViews(view: View) {
        incomeCategoryHolder = view.findViewById(R.id.ccdial_incomeCategories_rcv)
        expenseCategoryHolder = view.findViewById(R.id.ccdial_expenseCategories_rcv)
        dialogHolder = view.findViewById(R.id.ccdial_holder_crv)
        incomeSwitchButton = view.findViewById(R.id.astypeseldial_liability_btn)
        expenseSwitchButton = view.findViewById(R.id.ccdial_expence_btn)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerViews()
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        //incomeSwitchButton.setBackgroundColor(requireActivity().getColor(R.color.transparent))
        incomeSwitchButton.setOnClickListener { switchTransactionTypeVisibility(false) }
        expenseSwitchButton.setOnClickListener { switchTransactionTypeVisibility(true) }
    }

    private fun switchTransactionTypeVisibility(isExpenseSelected: Boolean) {
        incomeCategoryHolder.visibility = if (isExpenseSelected) View.GONE else View.VISIBLE
        expenseCategoryHolder.visibility = if (isExpenseSelected) View.VISIBLE else View.GONE
        incomeSwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(if (isExpenseSelected) R.color.grey else R.color.blue))
        expenseSwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(if (isExpenseSelected) R.color.blue else R.color.grey))
    }

    private fun setUpRecyclerViews() {
        incomeCategoryHolder.layoutManager = LinearLayoutManager(activity)
        val adapterIncome = CategoryRecyclerAdapter((AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.INCOME }).toMutableList(), requireActivity() as AppCompatActivity, true, true)
        adapterIncome.setOnItemClickListener { category -> returnResult(category) }
        incomeCategoryHolder.adapter = adapterIncome

        expenseCategoryHolder.layoutManager = LinearLayoutManager(activity)
        val adapterExpense = CategoryRecyclerAdapter((AustromApplication.activeCategories.values.filter {l -> l.transactionType==TransactionType.EXPENSE}).toMutableList(), requireActivity() as AppCompatActivity, true, true)
        adapterExpense.setOnItemClickListener { category -> returnResult(category)  }
        expenseCategoryHolder.adapter = adapterExpense
    }

    override fun onResume() {
        super.onResume()
        setUpRecyclerViews()
    }
}