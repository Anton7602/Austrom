package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Asset

class AssetRecyclerAdapter(private val assets: MutableMap<String, Asset>, var selectedItemPosition  : Int = -1) : RecyclerView.Adapter<AssetRecyclerAdapter.AssetViewHolder>()  {
    private var selectedHolder : AssetViewHolder? = null

    class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectionMarker: CardView = itemView.findViewById(R.id.asitem_selectionMarker_crv)
        val assetHolder: CardView = itemView.findViewById(R.id.asitem_assetHolder_crv)
        val assetName: TextView = itemView.findViewById(R.id.asitem_assetName_txt)
        val assetType: TextView = itemView.findViewById(R.id.asitem_assetType_txt)
        val assetAmount: TextView = itemView.findViewById(R.id.asitem_amount_txt)
        val currencySymbol: TextView = itemView.findViewById(R.id.asitem_currencySymbol_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_asset,parent,false)
        return AssetViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return assets.size
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = assets.entries.elementAt(position).value
        holder.assetName.text = asset.assetName
        holder.assetType.text = asset.assetTypeId.toString()
        holder.assetAmount.text = String.format("%.2f", asset.amount)
        holder.currencySymbol.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
        if (position == selectedItemPosition) {
            selectedHolder = holder
            holder.selectionMarker.visibility = View.VISIBLE
        }

        holder.assetHolder.setOnClickListener {
            switchItemSelection(holder, selectedItemPosition, position)
        }
    }

    private fun switchItemSelection(holder: AssetViewHolder, oldPosition: Int, newPosition: Int) {
        if (oldPosition>=0 && oldPosition<assets.size) {
            selectedHolder?.selectionMarker?.visibility = View.GONE
        }
        if (newPosition>=0 && newPosition<assets.size) {
            holder.selectionMarker.visibility = View.VISIBLE
        }
        selectedHolder = holder
        selectedItemPosition = newPosition
    }
}