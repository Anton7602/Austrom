package com.colleagues.austrom.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.views.MoneyFormatTextView

class AssetSquareRecyclerAdapter (private val assets: MutableList<Asset>, private val context: Context, private var selectedAsset: Asset? = null) : RecyclerView.Adapter<AssetSquareRecyclerAdapter.AssetSquareViewHolder>(){
    class AssetSquareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val assetHolder: CardView = itemView.findViewById(R.id.assqitem_assetHolder_crv)
        val assetName: TextView = itemView.findViewById(R.id.assqitem_assetName_txt)
        val assetAmount: MoneyFormatTextView = itemView.findViewById(R.id.assqitem_assetAmount_mont)
    }
    private var returnClickedItem: (Asset)->Unit = {}
    fun setOnItemClickListener(l: ((Asset)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetSquareViewHolder { return AssetSquareViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_asset_square,parent,false))  }
    override fun getItemCount(): Int { return assets.size }

    private var holderList = mutableListOf<AssetSquareViewHolder>()

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AssetSquareViewHolder, position: Int) {
        val asset = assets[position]
        holder.assetName.text = asset.assetName
        holder.assetAmount.setValue(asset.amount, AustromApplication.activeCurrencies[asset.currencyCode]?.symbol.toString())
        if (asset.assetId==selectedAsset?.assetId) {
            holder.assetHolder.setCardBackgroundColor(context.getColor(R.color.primaryTextColor));
            holder.assetName.setTextColor(context.getColor(R.color.backgroundMain))
        } else {
            holder.assetHolder.setCardBackgroundColor(context.getColor(R.color.transparent));
            holder.assetName.setTextColor(context.getColor(R.color.primaryTextColor))
        }
        holderList.add(holder)

        holder.assetHolder.setOnClickListener {
            selectedAsset = asset
            holderList.forEach { fholder -> fholder.assetHolder.setCardBackgroundColor(context.getColor(R.color.transparent)); fholder.assetName.setTextColor(context.getColor(R.color.primaryTextColor)) }
            holder.assetHolder.setCardBackgroundColor(context.getColor(R.color.primaryTextColor))
            holder.assetName.setTextColor(context.getColor(R.color.backgroundMain))
            returnClickedItem(asset)
        }
    }
}