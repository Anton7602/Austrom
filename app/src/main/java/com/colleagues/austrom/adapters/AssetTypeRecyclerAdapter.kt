package com.colleagues.austrom.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.AssetType

class AssetTypeRecyclerAdapter(private val assetTypes: List<AssetType>, private val context: Context): RecyclerView.Adapter<AssetTypeRecyclerAdapter.AssetTypeViewHolder>() {
    class AssetTypeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val assetTypeHolder: LinearLayout = itemView.findViewById(R.id.itasstype_assetTypeHolder_lly)
        val assetTypeName: TextView = itemView.findViewById(R.id.itasstype_assetTypeName_txt)
        val assetTypeDesc: TextView = itemView.findViewById(R.id.itasstype_assetTypeDescr_txt)
        val assetTypeIcon: ImageView = itemView.findViewById(R.id.itasstype_assetTypeIcon_img)
    }
    private var returnClickedItem: (AssetType)->Unit = {}
    fun setOnItemClickListener(l: ((AssetType)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetTypeViewHolder { return AssetTypeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_asset_type, parent, false))  }
    override fun getItemCount(): Int { return assetTypes.size  }

    override fun onBindViewHolder(holder: AssetTypeViewHolder, position: Int) {
        val assetType = assetTypes[position]
        holder.assetTypeName.text = context.getString(assetType.stringResourceId)
        holder.assetTypeDesc.text = context.getString(assetType.stringDescriptionResourceId)
        holder.assetTypeIcon.setImageResource(assetType.iconResourceId)

        holder.assetTypeHolder.setOnClickListener { returnClickedItem(assetType) }
    }
}