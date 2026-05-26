package com.example.plantscienceapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.plantscienceapp.R
import com.example.plantscienceapp.data.entity.Plant

/**
 * 植物列表适配器
 * 使用 ListAdapter 和 DiffUtil 实现高效的增量更新
 */
class PlantAdapter(
    private val onPlantClick: (Plant) -> Unit
) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(PlantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant_card, parent, false)
        return PlantViewHolder(view, onPlantClick)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlantViewHolder(
        itemView: View,
        private val onPlantClick: (Plant) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val ivPlantThumb: ImageView = itemView.findViewById(R.id.ivPlantThumb)
        private val tvPlantName: TextView = itemView.findViewById(R.id.tvPlantName)
        private val tvScientificName: TextView = itemView.findViewById(R.id.tvScientificName)
        private val tvPlantTags: TextView = itemView.findViewById(R.id.tvPlantTags)

        fun bind(plant: Plant) {
            tvPlantName.text = plant.name
            tvScientificName.text = plant.scientificName
            
            // 美化标签展示
            tvPlantTags.text = plant.tags.split(",")
                .filter { it.isNotBlank() }
                .joinToString("  |  ")

            // 【核心修改】：通过 imageName 获取资源 ID
            val context = itemView.context
            val resId = context.resources.getIdentifier(plant.imageName, "drawable", context.packageName)
            
            if (resId != 0) {
                ivPlantThumb.setImageResource(resId)
            } else {
                ivPlantThumb.setImageResource(R.drawable.ic_launcher_background)
            }

            // 设置点击回调
            itemView.setOnClickListener {
                onPlantClick(plant)
            }
        }
    }

    class PlantDiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem.plantId == newItem.plantId
        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean = oldItem == newItem
    }
}
