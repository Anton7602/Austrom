package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import com.colleagues.austrom.R
import com.colleagues.austrom.fragments.BalanceFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup


class AssetFilterDialogFragment(private val filteredFragment: BalanceFragment) : BottomSheetDialogFragment() {
    private lateinit var sharingModeGroup: MaterialButtonToggleGroup
    private lateinit var personalButton: Button
    private lateinit var sharedButton: Button
    private lateinit var dialogHolder: CardView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)
        applyFilter()

        val filterChangedListener = OnClickListener{
            filteredFragment.filterAssets(generateFilter())
        }

        personalButton.setOnClickListener(filterChangedListener)
        sharedButton.setOnClickListener(filterChangedListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_asset_filter, container, false)
    }

    private fun generateFilter() : AssetFilter {
        return  AssetFilter(
            showShared = sharingModeGroup.checkedButtonId==R.id.asfildial_shared_btn
        )
    }

    private fun applyFilter() {
        if (filteredFragment.activeFilter == null) return
        if (!filteredFragment.activeFilter!!.showShared) {
            sharingModeGroup.check(R.id.asfildial_personal_btn)
        }
    }

    private fun bindViews(view: View) {
        sharingModeGroup = view.findViewById(R.id.asfildial_sharingType_tgr)
        personalButton = view.findViewById(R.id.asfildial_personal_btn)
        sharedButton = view.findViewById(R.id.asfildial_shared_btn)
        dialogHolder = view.findViewById(R.id.asfildial_holder_crv)
    }
}

class AssetFilter(val showShared: Boolean)