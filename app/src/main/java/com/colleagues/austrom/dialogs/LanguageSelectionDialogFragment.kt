package com.colleagues.austrom.dialogs

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
import com.colleagues.austrom.adapters.LanguageRecyclerAdapter
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LanguageSelectionDialogFragment : BottomSheetDialogFragment(), IDialogInitiator {
    private lateinit var dialogHolder: CardView
    private lateinit var languagesHolder: RecyclerView
    private lateinit var declineButton: ImageButton
    private lateinit var searchField: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_language_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        setUpRecyclerView(listOf("English", "Russian"))

        declineButton.setOnClickListener {
            this.dismiss()
        }

        searchField.addTextChangedListener {
            languagesHolder.adapter = if (searchField.text.isNotEmpty()) {
                LanguageRecyclerAdapter(listOf("English", "Russian").filter { entry -> entry.contains(searchField.text, ignoreCase = true)}, requireActivity() as AppCompatActivity)
            } else {
                LanguageRecyclerAdapter(listOf("English", "Russian"), requireActivity() as AppCompatActivity)
            }
        }
    }

    override fun receiveValue(value: String, valueType: String) {
        (requireActivity().application as AustromApplication)
            .setNewBaseCurrency(AustromApplication.activeCurrencies[value]!!)
        dismiss()
    }

    private fun setUpRecyclerView(languagesList: List<String>) {
        languagesHolder.layoutManager = LinearLayoutManager(activity)
        languagesHolder.adapter = LanguageRecyclerAdapter(languagesList, requireActivity() as AppCompatActivity)
    }

    private fun bindViews(view: View) {
        languagesHolder = view.findViewById(R.id.lsdial_languageHolder_rcv)
        declineButton = view.findViewById(R.id.lsdial_close_btn)
        searchField = view.findViewById(R.id.lsdial_languageName_txt)
        dialogHolder = view.findViewById(R.id.lsdial_holder_crv)
    }
}