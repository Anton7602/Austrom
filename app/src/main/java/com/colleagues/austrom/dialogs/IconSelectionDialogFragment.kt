package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryIconRecyclerAdapter
import com.colleagues.austrom.managers.Icon
import com.colleagues.austrom.managers.IconManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class IconSelectionDialogFragment(private var selectedIcon: Icon? = null) : BottomSheetDialogFragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_icon_selection, container, false) }
    fun setOnDialogResultListener(l: (Icon)->Unit) { returnResult = l }
    private var returnResult: (Icon)->Unit = {}
    //region Binding
    private lateinit var dialogHolder: CardView
    private lateinit var closeButton: ImageButton
    private lateinit var iconHolder: RecyclerView
    private lateinit var searchField: CardView
    private fun bindViews(view: View) {
        dialogHolder = view.findViewById(R.id.icseldial_holder_crv)
        searchField = view.findViewById(R.id.icseldial_searchHolder_crv)
        closeButton = view.findViewById(R.id.icseldial_decline_btn)
        iconHolder = view.findViewById(R.id.icseldial_currencyholder_rcv)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerView()
        closeButton.setOnClickListener { dismiss() }
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        searchField.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
    }

    private fun setUpRecyclerView() {
        iconHolder.layoutManager = GridLayoutManager(activity, 6, LinearLayoutManager.VERTICAL, false)
        val adapter = CategoryIconRecyclerAdapter(IconManager().getAllAvailableIcons(),selectedIcon ?: Icon.I0)
        adapter.setOnItemClickListener { icon -> returnResult(icon); }
        iconHolder.adapter = adapter
    }
}