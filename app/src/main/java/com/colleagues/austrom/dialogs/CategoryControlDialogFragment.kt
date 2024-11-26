package com.colleagues.austrom.dialogs

import android.content.Intent
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
import com.colleagues.austrom.CategoryCreationActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryRecyclerAdapterNew
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CategoryControlDialogFragment(private val receiver: IDialogInitiator?) : BottomSheetDialogFragment() {
    private lateinit var incomeCategoryHolder: RecyclerView
    private lateinit var expenseCategoryHolder: RecyclerView
    private lateinit var dialogHolder: CardView
    private lateinit var incomeSwitchButton: Button
    private lateinit var expenseSwitchButton: Button
    private lateinit var addNewTransactionButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_category_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerViews()
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)


        incomeSwitchButton.setOnClickListener {
            incomeCategoryHolder.visibility = View.VISIBLE
            expenseCategoryHolder.visibility = View.GONE
        }

        expenseSwitchButton.setOnClickListener {
            incomeCategoryHolder.visibility = View.GONE
            expenseCategoryHolder.visibility = View.VISIBLE
        }

        addNewTransactionButton.setOnClickListener {
            requireActivity().startActivity(Intent(requireActivity(), CategoryCreationActivity::class.java))
        }

//        declineButton.setOnClickListener {
//            this.dismiss()
//        }
    }

    private fun setUpRecyclerViews() {
        val dbProvider = LocalDatabaseProvider(requireActivity())
        incomeCategoryHolder.layoutManager = LinearLayoutManager(activity)
        incomeCategoryHolder.adapter = CategoryRecyclerAdapterNew(dbProvider.getCategories(TransactionType.INCOME), requireActivity() as AppCompatActivity,  receiver)

        expenseCategoryHolder.layoutManager = LinearLayoutManager(activity)
        expenseCategoryHolder.adapter = CategoryRecyclerAdapterNew(dbProvider.getCategories(TransactionType.EXPENSE), requireActivity() as AppCompatActivity,  receiver)
    }

    private fun bindViews(view: View) {
        incomeCategoryHolder = view.findViewById(R.id.ccdial_incomeCategories_rcv)
        expenseCategoryHolder = view.findViewById(R.id.ccdial_expenseCategories_rcv)
        dialogHolder = view.findViewById(R.id.ccdial_holder_crv)
        incomeSwitchButton = view.findViewById(R.id.ccdial_income_btn)
        expenseSwitchButton = view.findViewById(R.id.ccdial_expence_btn)
        addNewTransactionButton = view.findViewById(R.id.ccdial_addNew_btn)
    }

    override fun onResume() {
        super.onResume()
        setUpRecyclerViews()
    }
}