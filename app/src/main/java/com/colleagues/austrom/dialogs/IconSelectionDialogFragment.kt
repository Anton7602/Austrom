package com.colleagues.austrom.dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryIconRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.managers.Icon
import com.colleagues.austrom.managers.IconManager
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class IconSelectionDialogFragment(private val receiver: IDialogInitiator? = null, private var selectedIcon: Icon? = null) : BottomSheetDialogFragment(), IDialogInitiator {
    private lateinit var dialogHolder: CardView
    private lateinit var closeButton: ImageButton
    private lateinit var iconHolder: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_icon_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerView()
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background)

        closeButton.setOnClickListener { dismiss() }
    }

    private fun setUpRecyclerView() {
        iconHolder.adapter = CategoryIconRecyclerAdapter(IconManager().getAllAvailableIcons(), this, selectedIcon ?: Icon.I0)
        iconHolder.layoutManager = GridLayoutManager(activity, 5, LinearLayoutManager.VERTICAL, false)
    }


    private fun bindViews(view: View) {
        dialogHolder = view.findViewById(R.id.icseldial_holder_crv)
        closeButton = view.findViewById(R.id.icseldial_decline_btn)
        iconHolder = view.findViewById(R.id.icseldial_currencyholder_rcv)
    }

    override fun receiveValue(value: String, valueType: String) {
        receiver?.receiveValue(value, valueType)
    }
}