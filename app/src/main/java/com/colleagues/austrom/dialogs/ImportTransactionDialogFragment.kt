package com.colleagues.austrom.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryArrayAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType


class ImportTransactionDialogFragment(private var transaction: Transaction,
                                      private var transactions: List<Transaction>,
                                      private var activity: AppCompatActivity,
                                      private var reciever: IDialogInitiator? = null) : DialogFragment() {
    private lateinit var acceptButton: Button
    private lateinit var cancelButton: Button
    private lateinit var categoryHolder: LinearLayout
    private lateinit var assetHolder: LinearLayout
    private lateinit var categoryLabel: TextView
    private lateinit var assetLabel: TextView
    private lateinit var knownCategories: Spinner
    private lateinit var knownAssets: Spinner
    private lateinit var unknownAsset: TextView
    private lateinit var unknownCategory: TextView


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val i: LayoutInflater = requireActivity().layoutInflater
        val view = i.inflate(R.layout.dialog_fragment_import_transaction, null)
        bindViews(view)
        val adb: AlertDialog.Builder = AlertDialog.Builder(requireActivity()).setView(view)
        val targetSelectionDialog = adb.create()
        if (targetSelectionDialog != null && targetSelectionDialog.window != null) {
            targetSelectionDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val activeAsset = if (AustromApplication.activeAssets.values.find { l -> l.assetName==transaction.targetName } != null) {
            AustromApplication.activeAssets.values.find { l -> l.assetName==transaction.targetName }
        } else {
            AustromApplication.activeAssets.values.find { l -> l.assetName==transaction.sourceName }
        }
        val categoriesList = if (transaction.amount>0) {
            AustromApplication.activeIncomeCategories.values.toList()
        } else {
            AustromApplication.activeExpenseCategories.values.toList()
        }
        knownCategories.adapter = CategoryArrayAdapter(activity, categoriesList)
        knownAssets.adapter = ArrayAdapter(activity, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, AustromApplication.activeAssets.map { it.value.assetName })
        unknownCategory.text = transaction.categoryId
        unknownAsset.text = transaction.targetName

        acceptButton.setOnClickListener {
            transactions.forEach { _transaction ->
                if (_transaction.categoryId == transaction.categoryId && _transaction.transactionType()==transaction.transactionType()) {
                    _transaction.categoryId = (knownCategories.selectedItem as Category).id
                    reciever?.receiveValue(transactions.indexOf(_transaction).toString(), "UpdatedTransactionIndex")
                }
            }
            transaction.categoryId = (knownCategories.selectedItem as Category).id
            reciever?.receiveValue(transactions.indexOf(transaction).toString(), "UpdatedTransactionIndex")
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return targetSelectionDialog
    }

    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.implinkdial_accept_btn)
        cancelButton = view.findViewById(R.id.implinkdial_cancel_btn)
        categoryLabel = view.findViewById(R.id.implinkdial_categoryLabel_txt)
        assetLabel = view.findViewById(R.id.implinkdial_assetLabel_txt)
        categoryHolder = view.findViewById(R.id.implinkdial_categoryHolder_lly)
        assetHolder = view.findViewById(R.id.implinkdial_assetHolder_lly)
        knownAssets = view.findViewById(R.id.implinkdial_knownAssets_spn)
        unknownAsset = view.findViewById(R.id.implinkdial_unknownAsset_txt)
        knownCategories = view.findViewById(R.id.implinkdial_knownCategories_spn)
        unknownCategory = view.findViewById(R.id.implinkdial_unknownCategory_txt)

    }
}