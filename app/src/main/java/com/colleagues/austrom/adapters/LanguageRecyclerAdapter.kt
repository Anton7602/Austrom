package com.colleagues.austrom.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.startWithUppercase
import java.util.Locale

class LanguageRecyclerAdapter(private val languages: List<Locale>, private val context: Context): RecyclerView.Adapter<LanguageRecyclerAdapter.LanguageViewHolder>() {
    class LanguageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var languageHolder: CardView = itemView.findViewById(R.id.lanit_languageHolder_crv)
        var languageName: TextView = itemView.findViewById(R.id.lanit_languageName_txt)
        var languageCode: TextView = itemView.findViewById(R.id.lanit_languageCode_txt)
        var selectionMarker: RadioButton = itemView.findViewById(R.id.lanit_selectionMarker)
    }
    private var returnClickedItem: (Locale)->Unit = {}
    fun setOnItemClickListener(l: ((Locale)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder { return LanguageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false))  }
    override fun getItemCount(): Int { return languages.size  }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val locale = languages[position]
        holder.languageName.text = locale.displayLanguage.startWithUppercase()
        holder.languageCode.text = locale.language.uppercase()
        holder.selectionMarker.isChecked = locale==context.resources.configuration.locales.get(0)
        holder.languageHolder.setOnClickListener {
            returnClickedItem(locale)
            //switchLanguage(languages[position])
        }
        holder.selectionMarker.setOnClickListener {
            returnClickedItem(locale)
            switchLanguage(languages[position])
        }
    }

    private fun switchLanguage(locale: Locale) {

    }
}