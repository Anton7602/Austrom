package com.colleagues.austrom.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.colleagues.austrom.AustromApplication.Companion.appUser
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryArrayAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Budget
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CategoryPullDialogFragment(private val deletedCategory: Category, private val transactionsToChange: List<Transaction>) : DialogFragment() {
    fun setOnDialogResultListener(l: ((Boolean)->Unit)) { returnResult = l }
    private var returnResult: (Boolean)->Unit = {}
    //region Binding
    private lateinit var acceptButton: Button
    private lateinit var cancelButton: Button
    private lateinit var messageLabel: TextView
    private lateinit var categoriesSpinner: Spinner
    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.catpull_delete_btn)
        cancelButton = view.findViewById(R.id.catpull_cancel_btn)
        messageLabel = view.findViewById(R.id.catpull_message_txt)
        categoriesSpinner = view.findViewById(R.id.catpull_categories_rcv)
    }
    // endregion

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val i: LayoutInflater = requireActivity().layoutInflater
        val view = i.inflate(R.layout.dialog_fragment_category_pull, null)
        bindViews(view)
        setUpValues()

        acceptButton.setOnClickListener { updateCategoryInTransactions(); dismiss() }
        cancelButton.setOnClickListener { dismiss() }

        return MaterialAlertDialogBuilder(requireContext()).setView(view).create()
    }

    private fun updateCategoryInTransactions() {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        var remoteDBProvider: IRemoteDatabaseProvider? = null
        var budget: Budget? = null
        if (appUser!!.activeBudgetId!=null) {
            remoteDBProvider = FirebaseDatabaseProvider(requireActivity())
            budget = remoteDBProvider.getBudgetById(appUser!!.activeBudgetId!!)
        }
        transactionsToChange.forEach { transaction ->
            transaction.categoryId = (categoriesSpinner.selectedItem as Category).categoryId
            localDBProvider.updateTransaction(transaction)
            if (remoteDBProvider!=null && budget!=null) remoteDBProvider.updateTransaction(transaction, budget)
        }
        returnResult(true)
    }

    private fun setUpValues() {
        val dbProvider = LocalDatabaseProvider(requireActivity())
        messageLabel.text = requireActivity().getString(R.string.transaction_Transfer_message, transactionsToChange.size.toString())
        val categories = dbProvider.getCategories(deletedCategory.transactionType).toMutableList()
        categories.remove(deletedCategory)
        categoriesSpinner.adapter = CategoryArrayAdapter(requireActivity(), categories)
    }
}