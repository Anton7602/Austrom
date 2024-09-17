package com.colleagues.austrom.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.AssetRecyclerAdapter
import com.colleagues.austrom.models.Asset


class AssetSelectionDialogFragment(private val isReturnToSource: Boolean = true,
    private var listOfAsset: MutableMap<String, Asset> = mutableMapOf(),
    private var parentDialog: TransactionCreationDialogFragment? = null ) : DialogFragment() {
        private lateinit var searchView: EditText
        private lateinit var assetsRecyclerView: RecyclerView
        private lateinit var emptyAssetsText: TextView
        private lateinit var acceptButton: Button
        private lateinit var cancelButton: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val i: LayoutInflater = requireActivity().layoutInflater
        val view = i.inflate(R.layout.dialog_fragment_asset_selection, null)
        bindViews(view)
        if (listOfAsset.isNotEmpty()) {
            searchView.visibility = View.VISIBLE
            assetsRecyclerView.visibility = View.VISIBLE
            acceptButton.visibility = View.VISIBLE
            emptyAssetsText.visibility = View.GONE
        }
        else {
            searchView.visibility = View.GONE
            assetsRecyclerView.visibility = View.GONE
            acceptButton.visibility = View.GONE
            emptyAssetsText.visibility = View.VISIBLE
        }
        assetsRecyclerView.layoutManager = LinearLayoutManager(activity)
        assetsRecyclerView.adapter = AssetRecyclerAdapter(AustromApplication.activeAssets, 0)
        val adb: AlertDialog.Builder = AlertDialog.Builder(requireActivity()).setView(view)
        val assetSelectionDialog = adb.create()
        if (assetSelectionDialog != null && assetSelectionDialog.window != null) {
            assetSelectionDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }

        acceptButton.setOnClickListener {
            if (parentDialog!=null) {
                val selectedAsset = AustromApplication.activeAssets.entries.elementAt((assetsRecyclerView.adapter as AssetRecyclerAdapter).selectedItemPosition).value
                if (isReturnToSource) {
                    parentDialog!!.receiveSourceSelection(selectedAsset, selectedAsset.assetName)
                } else {
                    parentDialog!!.receiveTargetSelection(selectedAsset, selectedAsset.assetName)
                }
            }
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
        return assetSelectionDialog
    }


    private fun bindViews(view: View) {
        searchView = view.findViewById(R.id.asdial_searchField_txt)
        assetsRecyclerView = view.findViewById(R.id.asdial_assetsHolder_rcv)
        emptyAssetsText = view.findViewById(R.id.asdial_noAssetsText_txt)
        acceptButton = view.findViewById(R.id.asdial_acceptButton_btn)
        cancelButton = view.findViewById(R.id.asdial_cancelButton_btn)
    }
}