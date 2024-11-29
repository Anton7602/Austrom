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
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryRecyclerAdapterNew
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.managers.Icon
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CategoryControlDialogFragment(private val receiver: IDialogInitiator?) : BottomSheetDialogFragment() {
    private lateinit var incomeCategoryHolder: RecyclerView
    private lateinit var expenseCategoryHolder: RecyclerView
    private lateinit var dialogHolder: CardView
    private lateinit var incomeSwitchButton: Button
    private lateinit var expenseSwitchButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_category_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerViews()
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)

        incomeSwitchButton.setBackgroundColor(requireActivity().getColor(R.color.transparent))
        incomeSwitchButton.setOnClickListener {
            incomeCategoryHolder.visibility = View.VISIBLE
            expenseCategoryHolder.visibility = View.GONE
            incomeSwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(R.color.blue))
            expenseSwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(R.color.grey))
        }

        expenseSwitchButton.setOnClickListener {
            incomeCategoryHolder.visibility = View.GONE
            expenseCategoryHolder.visibility = View.VISIBLE
            incomeSwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(R.color.grey))
            expenseSwitchButton.compoundDrawablesRelative[3].setTint(requireActivity().getColor(R.color.blue))
        }
    }

    private fun setUpRecyclerViews() {
        val dbProvider = LocalDatabaseProvider(requireActivity())

        val incomeTransactions = dbProvider.getCategories(TransactionType.INCOME).toMutableList()
        incomeTransactions.add(0, Category(transactionType = TransactionType.INCOME, name= getString(R.string.add_new_category), imgReference = Icon.I64, type = "Mandatory"))

        val expenseTransactions = (dbProvider.getCategories(TransactionType.EXPENSE)).toMutableList()
        expenseTransactions.add(0, Category(transactionType = TransactionType.EXPENSE, name= getString(R.string.add_new_category), imgReference = Icon.I64, type = "Mandatory"))

        incomeCategoryHolder.layoutManager = LinearLayoutManager(activity)
        incomeCategoryHolder.adapter = CategoryRecyclerAdapterNew(incomeTransactions, requireActivity() as AppCompatActivity,  receiver)

        expenseCategoryHolder.layoutManager = LinearLayoutManager(activity)
        expenseCategoryHolder.adapter = CategoryRecyclerAdapterNew(expenseTransactions, requireActivity() as AppCompatActivity,  receiver)
    }

    private fun bindViews(view: View) {
        incomeCategoryHolder = view.findViewById(R.id.ccdial_incomeCategories_rcv)
        expenseCategoryHolder = view.findViewById(R.id.ccdial_expenseCategories_rcv)
        dialogHolder = view.findViewById(R.id.ccdial_holder_crv)
        incomeSwitchButton = view.findViewById(R.id.ccdial_income_btn)
        expenseSwitchButton = view.findViewById(R.id.ccdial_expence_btn)
    }

    override fun onResume() {
        super.onResume()
        setUpRecyclerViews()
    }
}