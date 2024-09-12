package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Asset

class AssetRecyclerAdapter(private val assets: List<Asset>) : RecyclerView.Adapter<AssetRecyclerAdapter.AssetViewHolder>()  {
    class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val assetName: TextView = itemView.findViewById(R.id.asitem_assetName_txt)
        val assetType: TextView = itemView.findViewById(R.id.asitem_assetType_txt)
        val assetAmount: TextView = itemView.findViewById(R.id.asitem_amount_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_asset,parent,false)
        return AssetViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return assets.size
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.assetName.text = assets[position].assetName
        holder.assetType.text = assets[position].assetType_id.toString()
        holder.assetAmount.text = assets[position].amount.toString()
    }
}