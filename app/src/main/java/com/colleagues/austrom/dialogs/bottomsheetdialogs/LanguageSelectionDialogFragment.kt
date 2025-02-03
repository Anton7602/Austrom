package com.colleagues.austrom.dialogs.bottomsheetdialogs

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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class LanguageSelectionDialogFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { return inflater.inflate(R.layout.dialog_fragment_language_selection, container, false) }
    fun setOnDialogResultListener(l: ((Locale)->Unit)) { returnResult = l }
    private var returnResult: (Locale)->Unit = {}
    //region Binding
    private lateinit var dialogHolder: CardView
    private lateinit var languagesHolder: RecyclerView
    private lateinit var declineButton: ImageButton
    private lateinit var searchField: EditText
    private lateinit var searchHolder: CardView
    private fun bindViews(view: View) {
        languagesHolder = view.findViewById(R.id.lsdial_languageHolder_rcv)
        declineButton = view.findViewById(R.id.lsdial_close_btn)
        searchField = view.findViewById(R.id.lsdial_languageName_txt)
        dialogHolder = view.findViewById(R.id.lsdial_holder_crv)
        searchHolder = view.findViewById(R.id.lsdial_searchHolder_crv)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        searchHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        setUpRecyclerView(AustromApplication.supportedLanguages)
        declineButton.setOnClickListener { this.dismiss() }
        searchField.addTextChangedListener { filterLanguagesList(searchField.text.toString()) }
    }

    private fun filterLanguagesList(searchText: String) {
        val adapter = if (searchText.isNotEmpty()) {
            LanguageRecyclerAdapter(AustromApplication.supportedLanguages.filter { entry -> entry.displayLanguage.contains(searchText, ignoreCase = true)}, requireActivity() as AppCompatActivity)
        } else {
            LanguageRecyclerAdapter(AustromApplication.supportedLanguages, requireActivity() as AppCompatActivity)
        }
        adapter.setOnItemClickListener { locale -> returnResult(locale); dismiss() }
        languagesHolder.adapter = adapter
    }

    private fun setUpRecyclerView(languagesList: List<Locale>) {
        languagesHolder.layoutManager = LinearLayoutManager(activity)
        val adapter = LanguageRecyclerAdapter(languagesList, requireActivity() as AppCompatActivity)
        adapter.setOnItemClickListener { locale -> returnResult(locale); dismiss() }
        languagesHolder.adapter = adapter
    }
}